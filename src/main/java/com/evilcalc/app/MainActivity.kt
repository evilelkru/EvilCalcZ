package com.evilcalc.app

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var expressionDisplay: EditText
    private lateinit var resultMain: TextView
    private lateinit var resultBracket: TextView
    private lateinit var themeSwitch: Switch

    private var expression = ""
    private var cursorPosition = 0
    private lateinit var historyManager: HistoryManager

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        historyManager = HistoryManager(this)

        expressionDisplay = findViewById(R.id.expressionDisplay)
        resultMain = findViewById(R.id.resultMain)
        resultBracket = findViewById(R.id.resultBracket)
        themeSwitch = findViewById(R.id.themeSwitch)

        setupThemeSwitch()
        setupButtons()
        setupExpressionDisplay()
        updateDisplay()
        autoCalculate()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("evilcalc_prefs", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("isDarkTheme", true)
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupThemeSwitch() {
        val prefs = getSharedPreferences("evilcalc_prefs", Context.MODE_PRIVATE)
        themeSwitch.isChecked = !prefs.getBoolean("isDarkTheme", true)
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("isDarkTheme", !isChecked).apply()
            recreate()
        }
    }

    private fun setupExpressionDisplay() {
        expressionDisplay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString() ?: ""
                if (text != expression) {
                    expression = text
                    cursorPosition = expressionDisplay.selectionStart
                    autoCalculate()
                }
            }
        })
    }

    private fun setupButtons() {
        val buttons = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9",
            R.id.btnDot to ".", R.id.btnOpenParen to "(", R.id.btnCloseParen to ")",
            R.id.btnAdd to "+", R.id.btnSub to "−", R.id.btnMul to "×", R.id.btnDiv to "÷",
            R.id.btnPercent to "%"
        )

        buttons.forEach { (id, char) ->
            findViewById<Button>(id).setOnClickListener { inputChar(char) }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { backspace() }
        findViewById<Button>(R.id.btnDel).setOnClickListener { deleteChar() }
        findViewById<Button>(R.id.btnLeft).setOnClickListener { moveCursor(-1) }
        findViewById<Button>(R.id.btnRight).setOnClickListener { moveCursor(1) }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculate() }
        findViewById<Button>(R.id.btnHistory).setOnClickListener { 
            Toast.makeText(this, "История: " + historyManager.getHistory().size + " записей", Toast.LENGTH_SHORT).show() 
        }
    }

    private fun inputChar(c: String) {
        expression = expression.substring(0, cursorPosition) + c + expression.substring(cursorPosition)
        cursorPosition++
        updateDisplay()
        autoCalculate()
    }

    private fun backspace() {
        if (cursorPosition > 0) {
            expression = expression.substring(0, cursorPosition - 1) + expression.substring(cursorPosition)
            cursorPosition--
            updateDisplay()
            autoCalculate()
        }
    }

    private fun deleteChar() {
        if (cursorPosition < expression.length) {
            expression = expression.substring(0, cursorPosition) + expression.substring(cursorPosition + 1)
            updateDisplay()
            autoCalculate()
        }
    }

    private fun moveCursor(delta: Int) {
        cursorPosition += delta
        cursorPosition = cursorPosition.coerceIn(0, expression.length)
        expressionDisplay.setSelection(cursorPosition)
    }

    private fun updateDisplay() {
        expressionDisplay.setText(expression)
        expressionDisplay.setSelection(cursorPosition)
    }

    private fun formatNumber(num: Double): String {
        if (num.isNaN() || num.isInfinite()) return "0"
        val rounded = (num * 100).roundToInt() / 100.0
        val parts = rounded.toString().split(".")
        var intPart = parts[0]
        val decPart = if (parts.size > 1) "." + parts[1] else ""
        val negative = intPart.startsWith("-")
        if (negative) intPart = intPart.substring(1)

        val sb = StringBuilder()
        var count = 0
        for (i in intPart.length - 1 downTo 0) {
            sb.insert(0, intPart[i])
            count++
            if (count % 3 == 0 && i > 0) sb.insert(0, " ")
        }
        return (if (negative) "-" else "") + sb.toString() + decPart
    }

    private fun autoCalculate() {
        if (expression.isEmpty()) {
            resultMain.text = "0"
            resultBracket.text = ""
            return
        }
        val result = CalculatorLogic.calculateAll(expression)
        val formatted = formatNumber(result.current)
        resultMain.text = formatted
        if (result.percentAmount > 0) {
            resultBracket.text = "[${formatNumber(result.percentAmount)}]"
        } else {
            resultBracket.text = ""
        }
    }

    private fun calculate() {
        if (expression.isEmpty()) return
        val result = CalculatorLogic.calculateAll(expression)
        val fr = formatNumber(result.current)
        val fp = if (result.percentAmount > 0) formatNumber(result.percentAmount) else null
        historyManager.addItem(HistoryItem(expression, fr, fp))
        resultMain.text = fr
        resultBracket.text = if (fp != null) "[$fp]" else ""
    }

    private fun clearAll() {
        if (expression.isNotEmpty()) {
            val result = CalculatorLogic.calculateAll(expression)
            val fr = formatNumber(result.current)
            val fp = if (result.percentAmount > 0) formatNumber(result.percentAmount) else null
            historyManager.addItem(HistoryItem(expression, fr, fp))
        }
        expression = ""
        cursorPosition = 0
        updateDisplay()
        resultMain.text = "0"
        resultBracket.text = ""
    }
}
