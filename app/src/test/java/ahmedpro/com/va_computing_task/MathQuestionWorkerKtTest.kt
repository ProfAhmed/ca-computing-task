package ahmedpro.com.va_computing_task

import junit.framework.Assert.assertEquals
import org.junit.Test

class MathQuestionWorkerKtTest  {

    @Test
    fun calculationWithSum() {
        val num1 = "1"
        val num2 = "1"
        val operator = "+"

        //When
        val result = calculation(num1, num2, operator)
        //Then
        assertEquals(2, result)
    }

    @Test
    fun calculationWithMinus() {
        val num1 = "1"
        val num2 = "1"
        val operator = "-"

        //When
        val result = calculation(num1, num2, operator)
        //Then
        assertEquals(0, result)
    }

    @Test
    fun calculationWithDiv() {
        val num1 = "1"
        val num2 = "1"
        val operator = "/"

        //When
        val result = calculation(num1, num2, operator)
        //Then
        assertEquals(1, result)
    }

    @Test
    fun calculationWithTimes() {
        val num1 = "1"
        val num2 = "1"
        val operator = "*"

        //When
        val result = calculation(num1, num2, operator)
        //Then
        assertEquals(1, result)
    }
}