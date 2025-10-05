package com.example.healthyme.utils

import android.content.Context
import com.example.healthyme.models.Mood
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MoodStorage(context: Context) {

    private val prefs = context.getSharedPreferences("Mood_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save list of entries
    fun saveEntries(entries: List<Mood>) {
        val json = gson.toJson(entries)
        prefs.edit().putString("Mood_list", json).apply()
    }

    // Load list of entries
    fun loadEntries(): MutableList<Mood> {
        val json = prefs.getString("Mood_list", null)
        return if (json.isNullOrEmpty()) {
            mutableListOf()
        } else {
            try {
                val type = object : TypeToken<MutableList<Mood>>() {}.type
                gson.fromJson<MutableList<Mood>>(json, type) ?: mutableListOf()
            } catch (e: Exception) {
                e.printStackTrace()
                mutableListOf()
            }
        }
    }

    // Optional: clear all saved entries
    fun clearEntries() {
        prefs.edit().remove("Mood_list").apply()
    }
}
