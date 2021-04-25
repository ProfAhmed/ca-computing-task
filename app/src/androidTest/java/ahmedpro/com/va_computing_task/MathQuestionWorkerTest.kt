package ahmedpro.com.va_computing_task

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.testing.TestWorkerBuilder
import androidx.work.workDataOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executor
import java.util.concurrent.Executors

@RunWith(AndroidJUnit4::class)

class MathQuestionWorkerTest {
    private lateinit var context: Context
    private lateinit var executor: Executor
    val KEY_RESULT = "RESULT"

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        executor = Executors.newSingleThreadExecutor()
    }

    @Test
    fun doWork() {
        //Given
        val data = Data.Builder()
        data.putString("num1", "1")
        data.putString("num2", "2")
        data.putString("operator", "+")

        //When
        val worker = TestWorkerBuilder<MathQuestionWorker>(
            context = context,
            executor = executor,
            inputData = data.build()
        ).build()

        //Then
        val outputData = workDataOf(KEY_RESULT to calculation("1", "2", "+").toString())
        val result = worker.doWork()
        assertEquals(ListenableWorker.Result.success(outputData), result)

    }
}