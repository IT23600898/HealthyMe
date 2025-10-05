package com.example.healthyme.models

import java.util.UUID

data class Reminder(
    var id: String = UUID.randomUUID().toString(),
    var title: String?,
    var interval: Int,            // user-entered number (minutes in TEST_MODE, hours in production mode)
    var isActive: Boolean,
    var iconRes: Int,
    var triggerTimeMillis: Long = 0L   // 🔑 notification trigger time (future date & time)
)
