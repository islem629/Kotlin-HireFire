package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.example.myapplication.dto.*
import com.example.myapplication.network.RetrofitNestClient

class CvFormActivity : AppCompatActivity() {

    private var currentStep = 1   // 1 = personal, 2 = professional, 3 = education, 4 = optional

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cv_form)

        // Sections
        val sectionPersonal = findViewById<LinearLayout>(R.id.sectionPersonal)
        val sectionProfessional = findViewById<LinearLayout>(R.id.sectionProfessional)
        val sectionEducation = findViewById<LinearLayout>(R.id.sectionEducation)
        val sectionOptional = findViewById<LinearLayout>(R.id.sectionOptional)

        // Inputs
        val etFullName = findViewById<TextInputEditText>(R.id.etFullName)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)

        val etProfessionalTitle = findViewById<TextInputEditText>(R.id.etProfessionalTitle)

        val rgDegreeType = findViewById<RadioGroup>(R.id.rgDegreeType)
        val etDegreeSpeciality = findViewById<TextInputEditText>(R.id.etDegreeSpeciality)
        val etDegreeYear = findViewById<TextInputEditText>(R.id.etDegreeYear)

        val btnBackCv = findViewById<Button>(R.id.btnBackCv)
        val btnSaveCv = findViewById<Button>(R.id.btnSaveCv)

        // Initial visibility
        sectionPersonal.visibility = View.VISIBLE
        sectionProfessional.visibility = View.GONE
        sectionEducation.visibility = View.GONE
        sectionOptional.visibility = View.GONE
        btnSaveCv.text = "Next"

        // Back button
        btnBackCv.setOnClickListener { finish() }

        // Next / Save
        btnSaveCv.setOnClickListener {
            when (currentStep) {

                // STEP 1 → STEP 2
                1 -> {
                    if (validatePersonal(etFullName, etEmail, etPhone)) {
                        sectionPersonal.visibility = View.GONE
                        sectionProfessional.visibility = View.VISIBLE
                        currentStep = 2
                    }
                }

                // STEP 2 → STEP 3
                2 -> {
                    if (validateProfessional(etProfessionalTitle)) {
                        sectionProfessional.visibility = View.GONE
                        sectionEducation.visibility = View.VISIBLE
                        currentStep = 3
                    }
                }

                // STEP 3 → STEP 4
                3 -> {
                    if (validateEducation(rgDegreeType, etDegreeSpeciality, etDegreeYear)) {
                        sectionEducation.visibility = View.GONE
                        sectionOptional.visibility = View.VISIBLE
                        btnSaveCv.text = "Save CV"
                        currentStep = 4
                    }
                }

                // FINAL SAVE
                // FINAL SAVE
                4 -> {
                    if (
                        validatePersonal(etFullName, etEmail, etPhone) &&
                        validateProfessional(etProfessionalTitle) &&
                        validateEducation(rgDegreeType, etDegreeSpeciality, etDegreeYear)
                    ) {
                        saveCvToBackend(
                            etFullName,
                            etEmail,
                            etPhone,
                            etProfessionalTitle,
                            rgDegreeType,
                            etDegreeSpeciality,
                            etDegreeYear
                        )
                    }
                }

            }
        }
    }
    private fun saveCvToBackend(
        etFullName: TextInputEditText,
        etEmail: TextInputEditText,
        etPhone: TextInputEditText,
        etProfessionalTitle: TextInputEditText,
        rgDegreeType: RadioGroup,
        etDegreeSpeciality: TextInputEditText,
        etDegreeYear: TextInputEditText
    ) {
        // 1) Get userId + token from SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getLong("userId", -1L)
        val token = prefs.getString("token", null)

        if (userId == -1L || token.isNullOrEmpty()) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 2) Build simple strings for backend fields
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val title = etProfessionalTitle.text.toString().trim()

        val degreeTypeId = rgDegreeType.checkedRadioButtonId
        val degreeType = if (degreeTypeId != -1) {
            findViewById<RadioButton>(degreeTypeId).text.toString()
        } else {
            ""
        }
        val speciality = etDegreeSpeciality.text.toString().trim()
        val year = etDegreeYear.text.toString().trim()

        val personalInfo = "Name: $fullName\nEmail: $email\nPhone: $phone"
        val education = "$degreeType - $speciality ($year)"

        // 3) Create request object
        val cvRequest = CvRequest(
            user_id = userId.toInt(),
            headline = title,
            personal_info = personalInfo,
            technical_skills = null,
            soft_skills = null,
            work_experience = null,
            education = education,
            languages = null,
            certifications = null,
            projects = null,
            summary = null
        )

        // 4) Call API
        RetrofitNestClient.apiService.createCv(
            authHeader = "Bearer $token",
            cvRequest = cvRequest
        ).enqueue(object : retrofit2.Callback<CvResponse> {
            override fun onResponse(
                call: retrofit2.Call<CvResponse>,
                response: retrofit2.Response<CvResponse>
            ) {
                if (response.isSuccessful) {
                    Toast.makeText(this@CvFormActivity, "CV saved!", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this@CvFormActivity, AccueilActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@CvFormActivity,
                        "Error saving CV: ${response.code()}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<CvResponse>, t: Throwable) {
                Toast.makeText(
                    this@CvFormActivity,
                    "Network error: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }


    // ---------------------------
    // VALIDATIONS
    // ---------------------------

    private fun validatePersonal(
        etFullName: TextInputEditText,
        etEmail: TextInputEditText,
        etPhone: TextInputEditText
    ): Boolean {
        var isValid = true

        if (etFullName.text.isNullOrEmpty()) {
            etFullName.error = "Name is required"
            isValid = false
        }

        val email = etEmail.text.toString().trim()
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Invalid email"
            isValid = false
        }

        if (etPhone.text.isNullOrEmpty()) {
            etPhone.error = "Phone is required"
            isValid = false
        }

        return isValid
    }

    private fun validateProfessional(etProfessionalTitle: TextInputEditText): Boolean {
        if (etProfessionalTitle.text.isNullOrEmpty()) {
            etProfessionalTitle.error = "Professional title required"
            return false
        }
        return true
    }

    private fun validateEducation(
        rg: RadioGroup,
        etSpeciality: TextInputEditText,
        etYear: TextInputEditText
    ): Boolean {
        var isValid = true

        if (rg.checkedRadioButtonId == -1) {
            Toast.makeText(this, "Please choose a degree type", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (etSpeciality.text.isNullOrEmpty()) {
            etSpeciality.error = "Speciality is required"
            isValid = false
        }

        val year = etYear.text.toString().trim()
        if (year.isEmpty()) {
            etYear.error = "Graduation year required"
            isValid = false
        } else if (year.length != 4) {
            etYear.error = "Enter a valid year (e.g. 2024)"
            isValid = false
        }

        return isValid
    }
}
