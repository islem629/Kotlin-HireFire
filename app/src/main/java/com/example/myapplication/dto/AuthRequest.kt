package com.example.myapplication.dto

data class AuthRequest(
    val email: String,
    val password: String,
    val username: String? = null,
    val confirmPassword: String? = null
)

