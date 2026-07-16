package com.evilcalc.app

import kotlin.math.roundToInt

data class CalcResult(
    val current: Double,
    val percentAmount: Double = 0.0
)

object CalculatorLogic {

    fun parseExpression(e: String): String {
        return e.replace(Regex("(\\d)\\s+(\\d)"), "$1$2")
    }

    fun safeEval(expr: String): Double? {
        return try {
            val cleaned = expr.replace(Regex("[+\\-*/]$"), "").trim()
            if (cleaned.isEmpty()) return null
            // Используем ScriptEngine через простой парсер
            val result = SimpleEvaluator.eval(cleaned)
            if (result.isFinite()) result else null
        } catch (e: Exception) {
            null
        }
    }

    fun calculateAll(expression: String): CalcResult {
        if (expression.isEmpty()) return CalcResult(0.0)
        return try {
            var c = parseExpression(expression)
                .replace("÷", "/")
                .replace("×", "*")
                .replace("−", "-")

            if (!c.contains("%")) {
                val r = safeEval(c)
                return CalcResult(r ?: 0.0)
            }

            val percentIndex = c.indexOf('%')
            val beforePercent = c.substring(0, percentIndex)
            val afterPercent = c.substring(percentIndex + 1)

            val percentMatch = Regex("([+\\-])\\s*(\\d+\\.?\\d*)\\s*$").find(beforePercent)
            if (percentMatch == null) {
                val r = safeEval(beforePercent)
                return CalcResult(r ?: 0.0)
            }

            val percentOp = percentMatch.groupValues[1]
            val percentValue = percentMatch.groupValues[2].toDouble()
            val exprBefore = beforePercent.substring(0, percentMatch.range.first).trim()
            var baseValue = 0.0
            if (exprBefore.isNotEmpty()) {
                val er = safeEval(exprBefore)
                if (er != null) baseValue = er
            }

            val percentAmount = baseValue * (percentValue / 100.0)
            val afterBase = if (percentOp == "-") baseValue - percentAmount else baseValue + percentAmount

            if (afterPercent.isNotEmpty()) {
                val fullExpr = "$afterBase$afterPercent"
                val fr = safeEval(fullExpr)
                if (fr != null) return CalcResult(fr, percentAmount)
                return CalcResult(afterBase, percentAmount)
            }
            CalcResult(afterBase, percentAmount)
        } catch (e: Exception) {
            CalcResult(0.0)
        }
    }
}
