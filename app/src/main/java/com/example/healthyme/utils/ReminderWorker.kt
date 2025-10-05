package com.example.healthyme.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.healthyme.R

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = "Time for: $title ⏰"

        android.util.Log.d("ReminderWorker", "⏰ Worker running, notification for $title")

        showNotification(title, message)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val channelId = "reminder_channel_v1"
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 👉 Default notification sound from system
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, "Reminders", importance)
            channel.description = "Reminder notifications with sound & vibration"

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            channel.setSound(soundUri, audioAttributes)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(0, 250, 200, 250)

            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri) // 👈 default notification sound
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 200, 250))

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
