package com.example.myapplication.network

import com.example.myapplication.dto.CvRequest
import com.example.myapplication.dto.CvResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface CvApiService {

    @POST("cv")
    fun createCv(
        @Header("Authorization") authHeader: String,
        @Body cvRequest: CvRequest
    ): Call<CvResponse>
    @GET("cv/user/{userId}")
    fun getCvByUserId(
        @Path("userId") userId: Long
    ): Call<CvResponse>
}