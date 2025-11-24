package com.example.myapplication.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.example.myapplication.model.User
import com.example.myapplication.dto.*
interface ApiService {
    @POST("register")
    fun register(@Body request: AuthRequest): Call<AuthResponse>
    @POST("login")
    fun login(@Body request: AuthRequest): Call<AuthResponse>

}