package com.example.healthyme.utils

import android.content.Context
import com.example.healthyme.models.Habit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HabitsStorage(context: Context) {
    private val prefs = context.getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save full list
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString("habits_list", json).apply()
    }

    // Load list (empty if none saved yet)
    fun loadHabits(): MutableList<Habit> {
        val json = prefs.getString("habits_list", null) ?: return mutableListOf()
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        return gson.fromJson(json, type)
    }

    // Add a single habit safely
    fun addHabit(habit: Habit) {
        val habits = loadHabits()
        habits.add(habit)
        saveHabits(habits)
    }

    // Remove a specific habit
    fun removeHabit(habit: Habit) {
        val habits = loadHabits()
        habits.remove(habit)
        saveHabits(habits)
    }

    // Clear all habits
    fun clearHabits() {
        prefs.edit().remove("habits_list").apply()
    }
}
