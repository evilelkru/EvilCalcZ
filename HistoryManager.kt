package com.evilcalc.app

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class HistoryItem(
    val expression: String,
    val result: String,
    val percentAmount: String? = null
)

class HistoryManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("evilcalc_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getHistory(): MutableList<HistoryItem> {
        val json = prefs.getString("history", "[]") ?: "[]"
        val type = object : TypeToken<MutableList<HistoryItem>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveHistory(history: List<HistoryItem>) {
        val limited = history.take(100)
        prefs.edit().putString("history", gson.toJson(limited)).apply()
    }

    fun addItem(item: HistoryItem) {
        val history = getHistory()
        history.add(0, item)
        saveHistory(history)
    }

    fun removeItem(index: Int) {
        val history = getHistory()
        if (index in history.indices) {
            history.removeAt(index)
            saveHistory(history)
        }
    }

    fun clearAll() {
        saveHistory(emptyList())
    }
}
