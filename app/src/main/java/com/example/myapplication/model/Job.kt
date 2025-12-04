package com.example.myapplication.model

data class Job(
    val id: Long,
    val title: String,
    val description: String,
    val company: String,
    val dateDebut: String?,
    val dateExpired: String?,
    val companyEmail: String
)
