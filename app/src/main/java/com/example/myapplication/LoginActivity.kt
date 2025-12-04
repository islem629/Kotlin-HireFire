package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.dto.AuthRequest
import com.example.myapplication.dto.AuthResponse
import com.example.myapplication.dto.CvResponse
import com.example.myapplication.network.RetrofitSpringClient
import com.example.myapplication.network.RetrofitNestClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvGoToSignUp: TextView
    private lateinit var btnBack: Button

    private lateinit var authResponse: AuthResponse   // will be set after successful login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // IDs must match your activity_login.xml
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnSignUpSubmit)  // change if your id is different
        tvGoToSignUp = findViewById(R.id.tvGoToLogin)  // change if your id is different
        btnBack = findViewById(R.id.btnBackLogin)

        btnLogin.setOnClickListener { loginUser() }

        tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnBack.setOnClickListener { finish() }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        val request = AuthRequest(
            email = email,
            password = password,
            username = null,
            confirmPassword = null
        )

        RetrofitSpringClient.apiService.login(request)
            .enqueue(object : Callback<AuthResponse> {

                override fun onResponse(
                    call: Call<AuthResponse>,
                    response: Response<AuthResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()

                        if (body == null) {
                            Toast.makeText(
                                this@LoginActivity,
                                "Empty response from server",
                                Toast.LENGTH_LONG
                            ).show()
                            return
                        }

                        authResponse = body
                        val token = body.token
                        val userId = body.userId
                        val email = body.email

                        // Save token + userId locally
                        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
                        prefs.edit()
                            .putString("token", token)
                            .putLong("userId", userId)
                            .putString("email", email)
                            .apply()

                        // After login, check if this user already has a CV (Nest)
                        checkUserCv(userId)
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid credentials",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    Toast.makeText(
                        this@LoginActivity,
                        "Network error: ${t.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    private fun checkUserCv(userId: Long) {
        RetrofitNestClient.apiService.getCvByUserId(userId)
            .enqueue(object : Callback<CvResponse> {

                override fun onResponse(
                    call: Call<CvResponse>,
                    response: Response<CvResponse>
                ) {
                    if (response.isSuccessful) {
                        // ✅ CV exists → go to Accueil
                        val intent = Intent(this@LoginActivity, AccueilActivity::class.java)
                        intent.putExtra("token", authResponse.token)
                        intent.putExtra("userId", authResponse.userId)
                        intent.putExtra("email", authResponse.email)
                        intent.putExtra("username", authResponse.username)
                        startActivity(intent)
                        finish()
                    } else {
                        // ❌ No CV / 404 → go create CV
                        goToCvForm()
                    }
                }

                override fun onFailure(call: Call<CvResponse>, t: Throwable) {
                    // On error, also send user to create CV
                    goToCvForm()
                }
            })
    }

    private fun goToCvForm() {
        val intent = Intent(this@LoginActivity, CvFormActivity::class.java)
        intent.putExtra("token", authResponse.token)
        intent.putExtra("userId", authResponse.userId)
        intent.putExtra("email", authResponse.email)
        startActivity(intent)
        finish()
    }
}
