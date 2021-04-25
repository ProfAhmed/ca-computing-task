package ahmedpro.com.va_computing_task

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit


// locationRequest properties
private const val INTERVAL: Long = 500 * 900000
private const val FASTEST_INTERVAL: Long = 500 * 900000

class MainActivity : AppCompatActivity() {
    private lateinit var queue: Queue<String>
    val stringBuilderLiveData = MutableLiveData<java.lang.StringBuilder>()
    private lateinit var locationRequest: LocationRequest
    private var requestingLocationUpdates: Boolean = false
    private val REQUEST_CHECK_SETTINGS: Int = 2
    private val REQUEST_CODE_LOCATION_PERMISSION: Int = 1
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback = object : LocationCallback() {
        @SuppressLint("TimberArgCount")
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult ?: return
            locationResult.lastLocation
            for (location in locationResult.locations) {
                Log.i("New Last Location", location.toString())
                tvrLocation.text = location.toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queue = LinkedList<String>() //control current operations
        val stringBuilder = StringBuilder() // show current operations

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//init location result
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_LOW_POWER
        locationRequest.interval = INTERVAL
        locationRequest.fastestInterval = FASTEST_INTERVAL
//init location callback

        btnCalc.setOnClickListener {
            if (validation()) {
                // input data to worker
                val data = Data.Builder()
                data.putString("num1", etNum1.text.toString())
                data.putString("num2", etNum2.text.toString())
                data.putString("operator", etOperatore.text.toString())
                // add operations in queue
                queue.add(
                    etNum1.text.toString() + " "
                            + etOperatore.text.toString()
                            + " " + etNum2.text.toString()
                )

                // define work task
                val workTask: OneTimeWorkRequest =
                    OneTimeWorkRequestBuilder<MathQuestionWorker>()
                        .setInitialDelay(etDelay.text.toString().toLong(), TimeUnit.SECONDS)
                        .setInputData(data.build())
                        .build()
                //start new task worker
                WorkManager
                    .getInstance(this)
                    .beginUniqueWork(
                        "my_unique_work",
                        ExistingWorkPolicy.APPEND,
                        workTask
                    )
                    .enqueue()

                // get output from worker
                getResult(this, this, workTask.id)

                //update string builder
                queue.forEach { operation ->
                    stringBuilder.append(operation).append("\n")
                }

                stringBuilderLiveData.value = stringBuilder
                //clear string builder after appending complete
                stringBuilder.clear()
            }
        }

        btnLocation.setOnClickListener {
            requestPermission()
        }
        //show result
        stringBuilderLiveData.observe(this, Observer {
            tvCurrentOperations.text = it.toString()
        })
    }

    fun getResult(context: Context, owner: LifecycleOwner, id: UUID) {
        val KEY_RESULT = "RESULT"
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(id)
            .observe(owner, Observer {
                if (it.state == WorkInfo.State.SUCCEEDED) {
                    val result = it.outputData.getString(KEY_RESULT)
                    tvrResult.text = result
                    queue.poll()
                    val stringBuilder = StringBuilder()
                    queue.forEach { operation ->
                        stringBuilder.append(operation).append("\n")
                    }
                    stringBuilderLiveData.value = stringBuilder
                    stringBuilder.clear()
                }
            })
    }

    private fun validation(): Boolean {
        if (etOperatore.text.toString() != "+"
            && etOperatore.text.toString() != "-"
            && etOperatore.text.toString() != "/"
            && etOperatore.text.toString() != "*"
        ) {
            Toast.makeText(this, "enter right operator", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etDelay.text.toString().isEmpty()
            || etNum1.text.toString().isEmpty()
            || etOperatore.text.toString().isEmpty()
            || etNum2.text.toString().isEmpty()
        ) {
            Toast.makeText(this, "enter all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission", "TimberArgCount")
    private fun getCurrentLocation() {

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)

        val task = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            requestingLocationUpdates = true
            startLocationUpdates()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    Toast.makeText(
                        this, "Failed to show", Toast.LENGTH_LONG
                    ).show()
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun requestPermission() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED ||

            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                // show Ui dialog,explain why need this permission
                Toast.makeText(this, "Show Ui Dialog", Toast.LENGTH_LONG).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            }
        } else {
            // Permission has already been granted
            getCurrentLocation()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_LOCATION_PERMISSION -> {

                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {

                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        getCurrentLocation()
                    }
                } else {

                    Toast.makeText(
                        this,
                        "Permission is needed to detect you current location automatically",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}