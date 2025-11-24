package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.btnSignup).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        findViewById<View>(R.id.btnLogin).setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        findViewById<android.view.View>(R.id.btnGetStarted).setOnClickListener {
            Toast.makeText(this, "Get Started!", Toast.LENGTH_SHORT).show()
        }
        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            val prefs = getSharedPreferences("auth", MODE_PRIVATE)
            val userId = prefs.getLong("userId", -1L)
            val token = prefs.getString("token", null)

            if (userId == -1L || token.isNullOrEmpty()) {
                Toast.makeText(this, "Please create an account first", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
            } else {
                // ✅ User is logged in → he can create his CV
                val intent = Intent(this, CvFormActivity::class.java)
                startActivity(intent)
            }
        }


    }
}