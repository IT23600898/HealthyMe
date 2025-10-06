package com.example.healthyme.utils

import android.content.Context
import android.util.Log
import com.example.healthyme.models.Mood
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class MoodStorage(context: Context) {

    private val prefs = context.getSharedPreferences("moods", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val key = "mood_list"

    fun saveEntries(entries: List<Mood>) {
        try {
            val json = gson.toJson(entries)
            prefs.edit().putString(key, json).apply()
            Log.d("MoodStorage", "✅ Saved ${entries.size} moods successfully.")
        } catch (e: Exception) {
            Log.e("MoodStorage", "❌ Error saving moods: ${e.message}")
        }
    }

    fun loadEntries(): MutableList<Mood> {
        val json = prefs.getString(key, null)
        return if (json.isNullOrEmpty()) {
            Log.d("MoodStorage", "⚠️ No moods found in storage.")
            mutableListOf()
        } else {
            try {
                val type = object : TypeToken<MutableList<Mood>>() {}.type
                val list: MutableList<Mood>? = gson.fromJson(json, type)
                list ?: mutableListOf()
            } catch (e: JsonSyntaxException) {
                Log.e("MoodStorage", "❌ Corrupt data found. Clearing storage.")
                clearEntries()
                mutableListOf()
            } catch (e: Exception) {
                Log.e("MoodStorage", "❌ Error loading moods: ${e.message}")
                mutableListOf()
            }
        }
    }

    fun clearEntries() {
        prefs.edit().remove(key).apply()
        Log.d("MoodStorage", "🧹 Cleared all saved moods.")
    }
}
