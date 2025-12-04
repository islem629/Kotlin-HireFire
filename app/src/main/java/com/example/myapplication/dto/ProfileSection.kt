package com.example.myapplication

data class ProfileSection(
    val id: String,
    val title: String,
    var content: String,
    var isExpanded: Boolean = false
)
