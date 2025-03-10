package mmcs.assignment3_calculator.viewmodel

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import java.lang.IllegalArgumentException

class CalculatorViewModel() : BaseObservable(), Calculator {
    override var valueDisplay = ObservableField<String>("")
    override var operationDisplay = ObservableField<String>("")
    private val operation = """([+\-\*\/])""".toRegex()
    private val number = """(-?\d+(?:\.\d+)?)""".toRegex()
    private val expressionRegex = "$number$operation$number".toRegex()

    private val integer = """(-?\d+)""".toRegex()
    private val pointPossibleRegex = "($integer)|($number$operation$integer)".toRegex()

    private fun _compute(expression: String): Double {
        var expr = expression
        if (expr == "")
            return 0.0

        if (expr.last() == '.' || operation.matches("${expr.last()}"))
            expr = expr.dropLast(1)

        if (number.matches(expr)) {
            return expr.toDouble()
        }

        val match = expressionRegex.matchEntire(expr) ?: throw Exception("compute error")
        val left = match.groupValues[1].toDouble()

        val right = match.groupValues[3].toDouble()

        val result = when (match.groupValues[2]) {
            "+" -> left + right
            "-" -> left - right
            "*" -> left * right
            "/" ->  if (right - 0.0 > 1e-10) left / right else throw IllegalArgumentException("Zero division")
            else -> {throw Exception("Unknown operation")}
        }

        return result
    }

    override fun addDigit(dig: Int) {
        val opValue = operationDisplay.get() ?: return
        operationDisplay.set(opValue + dig.toString())
    }

    override fun addPoint() {
        val opValue = operationDisplay.get() ?: return

        if (pointPossibleRegex.matches(opValue))
            operationDisplay.set("$opValue.")
    }

    override fun backspace() {
        val opValue = operationDisplay.get() ?: return
        operationDisplay.set(opValue.dropLast(1))

    }

    override fun addOperation(op: Operation) {

        val opValue = operationDisplay.get() ?: return

        var newValue: Double;

        try {
            newValue = _compute(opValue)
        }
        catch (e: IllegalArgumentException) {
            valueDisplay.set(e.message)
            operationDisplay.set("")
            return
        }

        if (op == Operation.PERCENT) {
            newValue /= 100
            valueDisplay.set(newValue.toString())
            operationDisplay.set(newValue.toString())

            return
        }

        valueDisplay.set(newValue.toString())
        operationDisplay.set(newValue.toString() + op.str)
    }

    override fun toggleSign() {
        val opValue = operationDisplay.get() ?: return

        val newValue = _compute(opValue)

        valueDisplay.set((newValue*-1).toString())
        operationDisplay.set((newValue*-1).toString())
    }

    override fun compute() {
        val opValue = operationDisplay.get() ?: return

        val newValue: Double
        try {
            newValue = _compute(opValue)
        }
        catch (e: IllegalArgumentException) {
            valueDisplay.set(e.message)
            operationDisplay.set("")
            return
        }

        valueDisplay.set(newValue.toString())
        operationDisplay.set(newValue.toString())
    }

    override fun clear() {
        operationDisplay.set("")
        valueDisplay.set("")
    }

    override fun reset() {
        operationDisplay.set("")
        valueDisplay.set("")
    }
}