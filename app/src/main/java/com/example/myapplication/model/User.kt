package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class User (@SerializedName("id") val id: Int? = null,
                 @SerializedName("name") val name: String?=null,
                 @SerializedName("email") val email: String?=null,
                 @SerializedName("role") val role: String?=null,
                 @SerializedName("enabled") val enabled: String?=null
)