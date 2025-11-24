package com.example.myapplication.dto

data class CvRequest(
    val user_id: Int,
    val headline: String?,
    val personal_info: String?,
    val technical_skills: String?,
    val soft_skills: String?,
    val work_experience: String?,
    val education: String?,
    val languages: String?,
    val certifications: String?,
    val projects: String?,
    val summary: String?
)