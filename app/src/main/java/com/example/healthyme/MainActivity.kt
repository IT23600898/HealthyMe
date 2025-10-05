package com.example.healthyme

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.healthyme.ui.HabitsFragment
import com.example.healthyme.ui.MoodFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // ✅ Default fragment load (HabitsFragment)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HabitsFragment())
                .commit()
        }

        // ✅ Handle Bottom Navigation
        bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_habits -> HabitsFragment()
                R.id.nav_mood -> MoodFragment()
                R.id.nav_reminder -> ReminderFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> HabitsFragment()
            }

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragment_container, fragment)
                .commit()

            true
        }

        // ✅ Handle intent from Welcome screen to open a specific tab
        val openFragment = intent.getStringExtra("openFragment")
        if (openFragment == "habits") {
            bottomNav.selectedItemId = R.id.nav_habits
        }

        // ✅ Runtime permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }
}
