package com.example.healthyme.models

data class Habit(
    val id: Int,
    val title: String,
    val time: String,
    var isCompleted: Boolean
)
