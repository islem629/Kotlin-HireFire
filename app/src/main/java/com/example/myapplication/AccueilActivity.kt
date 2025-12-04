package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.model.Job
import com.example.myapplication.network.RetrofitJobClient
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.myapplication.dto.*

class AccueilActivity : AppCompatActivity() {

    // 👉 Now we store backend Job objects directly
    private val jobs = mutableListOf<Job>()
    private var currentIndex = 0

    // Views
    private lateinit var cardJob: CardView
    private lateinit var tvJobMatches: TextView
    private lateinit var tvSubtitle: TextView
    private lateinit var tvJobTitle: TextView
    private lateinit var tvCompany: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvCompanyTag: TextView
    private lateinit var tvSwipeHint: TextView
    private lateinit var btnAccept: ImageView
    private lateinit var btnReject: ImageView
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var authResponse: AuthResponse

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)
        topAppBar = findViewById(R.id.topAppBar)
        val token = intent.getStringExtra("token") ?: ""
        val userId = intent.getLongExtra("userId", -1L)
        val email = intent.getStringExtra("email") ?: ""
        val username = intent.getStringExtra("username") ?: ""
        authResponse = AuthResponse(
            token = token,
            userId = userId,
            email = email,
            username=username
        )
        setupTopBar()

        bindViews()
        fetchRecommendedJobs()
    }

    private fun bindViews() {
        cardJob = findViewById(R.id.cardJob)
        tvJobMatches = findViewById(R.id.tvJobMatches)
        tvSubtitle = findViewById(R.id.tvSubtitle)
        tvJobTitle = findViewById(R.id.tvJobTitle)
        tvCompany = findViewById(R.id.tvCompany)
        tvLocation = findViewById(R.id.tvLocation)
        tvDescription = findViewById(R.id.tvDescription)
        tvCompanyTag = findViewById(R.id.tvCompanyTag)
        tvSwipeHint = findViewById(R.id.tvSwipeHint)
        btnAccept = findViewById(R.id.btnAccept)
        btnReject = findViewById(R.id.btnReject)

        btnAccept.setOnClickListener { onAcceptJob() }
        btnReject.setOnClickListener { onRejectJob() }
    }

    // -----------------------------
    // Like / Dislike
    // -----------------------------
    private fun onAcceptJob() {
        if (currentIndex < jobs.size) {
            val job = jobs[currentIndex]
            applyToJob(job)   // use coroutine version
        }
    }

    private fun onRejectJob() {
        if (currentIndex < jobs.size) {
            val job = jobs[currentIndex]
            showToast("You skipped ${job.title}")
            showNextJob()
        }
    }

    private fun showNextJob() {
        currentIndex++

        if (currentIndex >= jobs.size) {
            showNoMoreJobs()
        } else {
            showJob(jobs[currentIndex])
        }
    }

    private fun showJob(job: Job) {
        tvJobTitle.text = job.title
        tvCompany.text = job.company
        tvLocation.text = "Remote"          // backend has no location, so default
        tvDescription.text = job.description
        tvCompanyTag.text = "Company : ${job.company}"

        btnAccept.isEnabled = true
        btnReject.isEnabled = true
        btnAccept.alpha = 1f
        btnReject.alpha = 1f
        tvSwipeHint.text = "Swipe or use buttons below"
    }

    private fun showNoMoreJobs() {
        tvJobTitle.text = "No more matches"
        tvCompany.text = ""
        tvLocation.text = ""
        tvDescription.text = "Come back later to see new opportunities."
        tvCompanyTag.text = ""

        btnAccept.isEnabled = false
        btnReject.isEnabled = false
        btnAccept.alpha = 0.3f
        btnReject.alpha = 0.3f
        tvSwipeHint.text = "No more jobs to review"
    }

    // -----------------------------
    // Fetch recommended jobs (still using enqueue here)
    // -----------------------------
    private fun fetchRecommendedJobs() {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            showToast("Please log in first to see recommendations")
            return
        }

        RetrofitJobClient.apiService.getRecommendedJobs("Bearer $token")
            .enqueue(object : Callback<List<Job>> {
                override fun onResponse(
                    call: Call<List<Job>>,
                    response: Response<List<Job>>
                ) {
                    if (response.isSuccessful) {
                        val jobList = response.body() ?: emptyList()

                        jobs.clear()
                        jobs.addAll(jobList)   // 👈 keep Job objects directly

                        currentIndex = 0
                        if (jobs.isNotEmpty()) {
                            showJob(jobs[currentIndex])
                        } else {
                            showNoMoreJobs()
                        }
                    } else {
                        showToast("Error loading jobs: ${response.code()}")
                        showNoMoreJobs()
                    }
                }

                override fun onFailure(call: Call<List<Job>>, t: Throwable) {
                    showToast("Network error: ${t.message}")
                    showNoMoreJobs()
                }
            })
    }

    // -----------------------------
    // Apply to job with lifecycleScope + suspend endpoint
    // -----------------------------
    private fun applyToJob(job: Job) {
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            showToast("Please log in first")
            return
        }

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitJobClient.apiService.applyToJob(job.id, "Bearer $token")
                }

                if (response.isSuccessful) {
                    showToast("Application sent for ${job.title}")
                    showNextJob()
                } else {
                    showToast("Failed to apply: ${response.code()}")
                }
            } catch (e: Exception) {
                showToast("Network error: ${e.message}")
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun setupTopBar() {
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_profile -> {
                    openUserProfile()
                    true
                }
                R.id.action_logout -> {
                    performLogout()
                    true
                }
                else -> false
            }
        }
    }

    private fun openUserProfile() {
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("token", authResponse.token)
        intent.putExtra("userId", authResponse.userId)
        intent.putExtra("email", authResponse.email)
        intent.putExtra("username", authResponse.username)
        // if you add username later:
        // intent.putExtra("username", someUsername)
        startActivity(intent)
    }

    private fun performLogout() {
        // example: clear saved token from SharedPreferences if you have it
        // getSharedPreferences("auth", MODE_PRIVATE).edit().clear().apply()

        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

        // Go back to LoginActivity (or whatever your start screen is)
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}

