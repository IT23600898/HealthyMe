package com.example.healthyme

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class Welcome_Screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_screen)

        val btnGetStarted = findViewById<MaterialButton>(R.id.btnGetStarted)
        val btnLearnMore = findViewById<MaterialButton>(R.id.btnLearnMore)

        // ✅ Navigate to MainActivity (open HabitsFragment tab)
        btnGetStarted.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("openFragment", "habits")
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }

        btnLearnMore.setOnClickListener {
            // Optionally navigate to AboutActivity or show info
            // startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}
