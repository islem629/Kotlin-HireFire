package com.example.myapplication.dto

data class CvResponse(
    val id: Int,
    val user_id: Int,
    val headline: String?,
    val personal_info: String?,
    val education: String?
    // add more fields if you want
)
