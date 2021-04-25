package ahmedpro.com.va_computing_task

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf

class MathQuestionWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val KEY_RESULT = "RESULT"

    override fun doWork(): Result {

        // Do the work here--in this case, upload the images.

        val num1 = inputData.getString("num1")
        val num2 = inputData.getString("num2")
        val operator = inputData.getString("operator")
        val outputData = workDataOf(KEY_RESULT to calculation(num1, num2, operator).toString())

        // Indicate whether the work finished successfully with the Result
        return Result.success(outputData)
    }
}

fun calculation(num1: String?, num2: String?, operator: String?): Int {
    var result = 0
    when (operator) {
        "+" -> result = num1?.toInt()?.plus(num2?.toInt()!!)!!
        "-" -> result = num1?.toInt()?.minus(num2?.toInt()!!)!!
        "/" -> result = num1?.toInt()?.div(num2?.toInt()!!)!!
        "*" -> result = num1?.toInt()?.times(num2?.toInt()!!)!!
    }
    return result
}