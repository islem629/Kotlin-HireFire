package com.example.myapplication.dto

data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val username: String
)
