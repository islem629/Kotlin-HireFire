package com.example.myapplication

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.dto.CvResponse
import com.example.myapplication.network.RetrofitNestClient
import com.google.android.material.appbar.MaterialToolbar
import android.view.animation.AnimationUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserProfileActivity : AppCompatActivity() {

    // UI
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var tvUsername: TextView
    private lateinit var tvEmail: TextView
    private lateinit var recyclerView: RecyclerView

    // Recycler
    private val sections = mutableListOf<ProfileSection>()
    private lateinit var adapter: ProfileSectionAdapter

    // Data from AccueilActivity
    private var authToken: String? = null
    private var userId: Long = -1L
    private var emailFromIntent: String? = null
    private var usernameFromIntent: String? = null   // optional if you send it later

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // 1) Bind views
        topAppBar = findViewById(R.id.topAppBar)
        tvUsername = findViewById(R.id.tvUsername)
        tvEmail = findViewById(R.id.tvEmail)
        recyclerView = findViewById(R.id.recyclerProfileSections)

        // 2) Get data from intent
        initFromIntent()

        // 3) Setup UI
        setupToolbar()
        setupRecyclerView()
        loadUserHeader()

        // 4) Load CV from backend and animate
        loadUserCv()
    }

    // Read data passed from AccueilActivity
    private fun initFromIntent() {
        authToken = intent.getStringExtra("token")
        userId = intent.getLongExtra("userId", -1L)
        emailFromIntent = intent.getStringExtra("email")
        usernameFromIntent = intent.getStringExtra("username") // null if not sent
    }

    private fun setupToolbar() {
        // back arrow
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        // optional menu actions (e.g. expand all)
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_profile -> {
                    sections.forEach { it.isExpanded = true }
                    adapter.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = ProfileSectionAdapter(sections) { section, position ->
            showEditDialog(section, position)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // attach layout animation (profile card list animation)
        val animation = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_fall_down)
        recyclerView.layoutAnimation = animation
    }

    private fun loadUserHeader() {
        // username is optional for now, so fallback to userId
        tvUsername.text = usernameFromIntent ?: "User #$userId"
        tvEmail.text = emailFromIntent ?: "No email"
    }

    // 🔥 Backend call: get CV by userId
    private fun loadUserCv() {
        if (userId == -1L) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
            buildSectionsFromCv(null)
            return
        }

       RetrofitNestClient.apiService.getCvByUserId(userId).enqueue(object : Callback<CvResponse> {
            override fun onResponse(call: Call<CvResponse>, response: Response<CvResponse>) {
                if (response.isSuccessful) {
                    val cv = response.body()
                    buildSectionsFromCv(cv)
                } else {
                    Toast.makeText(
                        this@UserProfileActivity,
                        "Failed to load CV (${response.code()})",
                        Toast.LENGTH_SHORT
                    ).show()
                    buildSectionsFromCv(null)
                }
            }

            override fun onFailure(call: Call<CvResponse>, t: Throwable) {
                Toast.makeText(
                    this@UserProfileActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
                buildSectionsFromCv(null)
            }
        })
    }

    // 🧠 Build one expandable section per field in CvResponse
    private fun buildSectionsFromCv(cv: CvResponse?) {
        sections.clear()

        sections.add(
            ProfileSection(
                id = "headline",
                title = "Professional Headline",
                content = cv?.headline ?: "Add a short professional tagline about yourself."
            )
        )

        sections.add(
            ProfileSection(
                id = "personal_info",
                title = "Personal Information",
                content = cv?.personal_info ?: "Age, location, phone, etc."
            )
        )

        sections.add(
            ProfileSection(
                id = "technical_skills",
                title = "Technical Skills",
                content = cv?.technical_skills ?: "List your programming languages, frameworks, tools..."
            )
        )

        sections.add(
            ProfileSection(
                id = "soft_skills",
                title = "Soft Skills",
                content = cv?.soft_skills ?: "Communication, teamwork, problem-solving..."
            )
        )

        sections.add(
            ProfileSection(
                id = "work_experience",
                title = "Work Experience",
                content = cv?.work_experience ?: "Describe your past jobs, internships, and roles."
            )
        )

        sections.add(
            ProfileSection(
                id = "education",
                title = "Education",
                content = cv?.education ?: "Schools, degrees, GPA, relevant coursework."
            )
        )

        sections.add(
            ProfileSection(
                id = "languages",
                title = "Languages",
                content = cv?.languages ?: "List the languages you speak and your proficiency."
            )
        )

        sections.add(
            ProfileSection(
                id = "certifications",
                title = "Certifications",
                content = cv?.certifications ?: "Professional certifications, online courses..."
            )
        )

        sections.add(
            ProfileSection(
                id = "projects",
                title = "Projects",
                content = cv?.projects ?: "Highlight your personal and academic projects."
            )
        )

        sections.add(
            ProfileSection(
                id = "summary",
                title = "Summary",
                content = cv?.summary ?: "Summarize your profile and career goals."
            )
        )

        adapter.notifyDataSetChanged()
        recyclerView.scheduleLayoutAnimation()
    }

    // ✏️ Dialog to edit a section locally (later you can send update to backend)
    private fun showEditDialog(section: ProfileSection, position: Int) {
        val input = EditText(this).apply {
            setText(section.content)
            setSelection(text.length)
            minLines = 3
            maxLines = 6
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        AlertDialog.Builder(this)
            .setTitle("Edit ${section.title}")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val newText = input.text.toString().ifBlank { "—" }
                section.content = newText
                adapter.notifyItemChanged(position)

                // TODO: here you can call an API to update CV on backend
                // using authToken: "Bearer $authToken"
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
