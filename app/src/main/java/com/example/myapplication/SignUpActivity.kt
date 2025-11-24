package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.dto.AuthRequest
import com.example.myapplication.dto.AuthResponse
import com.example.myapplication.network.RetrofitSpringClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignUpActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etLastName: EditText
    private lateinit var btnSignUp: Button
    private lateinit var tvGoToLogin: TextView
    private lateinit var btnBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup) // verify file name

        // Match IDs from your signup XML
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnSignUp = findViewById(R.id.btnLoginSubmit)
        tvGoToLogin = findViewById(R.id.tvGoToSignUp)
        btnBack = findViewById(R.id.btnBackLogin)

        btnSignUp.setOnClickListener { registerUser() }

        tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun registerUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val username = etLastName.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AuthRequest(
            email = email,
            password = password,
            username = username,
            confirmPassword = confirmPassword
        )

        RetrofitSpringClient.apiService.register(request)
            .enqueue(object : Callback<AuthResponse> {
                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Account created!",
                            Toast.LENGTH_LONG
                        ).show()

                        // Either auto-login here or go to login screen:
                        startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registration failed: " + response.errorBody()?.string(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}
