package com.example.myapplication

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.myapplication.model.Job
import com.example.myapplication.network.RetrofitJobClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccueilActivity : AppCompatActivity() {

    private val jobs = mutableListOf<JobModel>()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accueil)

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

    private fun onAcceptJob() {
        if (currentIndex < jobs.size) {
            val job = jobs[currentIndex]
            showToast("You liked ${job.title}")
            showNextJob()
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

    private fun showJob(job: JobModel) {
        tvJobTitle.text = job.title
        tvCompany.text = job.company
        tvLocation.text = job.location
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
    // 🔥 Fetch recommended jobs via Retrofit
    // -----------------------------
    private fun fetchRecommendedJobs() {
        // 1) Get token from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val token = prefs.getString("token", null)

        if (token.isNullOrEmpty()) {
            showToast("Please log in first to see recommendations")
            // Optional: loadFakeJobs() as fallback if you want
            return
        }

        // 2) Call backend
        RetrofitJobClient.apiService.getRecommendedJobs("Bearer $token")
            .enqueue(object : Callback<List<Job>> {
                override fun onResponse(
                    call: Call<List<Job>>,
                    response: Response<List<Job>>
                ) {
                    if (response.isSuccessful) {
                        val jobList = response.body() ?: emptyList()

                        jobs.clear()

                        // Convert backend Job → UI JobModel
                        for (job in jobList) {
                            val jobModel = JobModel(
                                title = job.title,
                                company = job.company,
                                location = "Remote", // no location in backend, so default
                                description = job.description
                            )
                            jobs.add(jobModel)
                        }

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

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
