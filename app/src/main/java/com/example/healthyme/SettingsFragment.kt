package com.example.healthyme

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.work.WorkManager

class SettingsFragment : Fragment() {

    private lateinit var imgProfile: ImageView
    private lateinit var txtUserName: TextView
    private lateinit var txtUserEmail: TextView
    private lateinit var btnEditProfile: Button

    private lateinit var switchNotifications: SwitchCompat
    private lateinit var switchVibration: SwitchCompat
    private lateinit var switchDarkMode: SwitchCompat
    private lateinit var btnChangeSound: Button
    private lateinit var btnClearReminders: Button
    private lateinit var btnAbout: Button

    private val PREFS_NAME = "settings_prefs"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Bind Views
        imgProfile = view.findViewById(R.id.imgProfile)
        txtUserName = view.findViewById(R.id.txtUserName)
        txtUserEmail = view.findViewById(R.id.txtUserEmail)
        btnEditProfile = view.findViewById(R.id.btnEditProfile)

        switchNotifications = view.findViewById(R.id.switchNotifications)
        switchVibration = view.findViewById(R.id.switchVibration)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        btnChangeSound = view.findViewById(R.id.btnChangeSound)
        btnClearReminders = view.findViewById(R.id.btnClearReminders)
        btnAbout = view.findViewById(R.id.btnAbout)

        loadSettings()

        // Edit profile
        btnEditProfile.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Edit Profile")
                .setMessage("Profile editing not implemented yet.")
                .setPositiveButton("OK", null)
                .show()
        }

        // Notification switches
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            saveBoolean("notifications", isChecked)
        }
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            saveBoolean("vibration", isChecked)
        }
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            saveBoolean("darkmode", isChecked)
            // TODO: apply dark mode toggle
        }

        // Change sound
        btnChangeSound.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Notification Tone")
            startActivityForResult(intent, 100)
        }

        // Clear reminders
        btnClearReminders.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Reminders")
                .setMessage("Are you sure you want to delete all reminders?")
                .setPositiveButton("Yes") { _, _ ->
                    val prefs = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)
                    prefs.edit().clear().apply()

                    WorkManager.getInstance(requireContext()).cancelAllWork()

                    Log.d("SettingsFragment", "✅ All reminders cleared")
                }
                .setNegativeButton("No", null)
                .show()
        }

        // About App
        btnAbout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("About App")
                .setMessage("HealthyMe App\nVersion 1.0\nDeveloped by Me ❤️")
                .setPositiveButton("OK", null)
                .show()
        }

        return view
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        switchNotifications.isChecked = prefs.getBoolean("notifications", true)
        switchVibration.isChecked = prefs.getBoolean("vibration", true)
        switchDarkMode.isChecked = prefs.getBoolean("darkmode", false)
    }

    private fun saveBoolean(key: String, value: Boolean) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && data != null) {
            val uri: Uri? = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                saveString("notification_sound", uri.toString())
                Log.d("SettingsFragment", "🔔 New sound set: $uri")
            }
        }
    }

    private fun saveString(key: String, value: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(key, value).apply()
    }
}
