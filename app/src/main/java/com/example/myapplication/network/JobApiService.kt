package com.example.myapplication.network

import com.example.myapplication.model.Job
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header

interface JobApiService {
    @GET("jobs/recommendations")
    fun getRecommendedJobs(
        @Header("Authorization") authHeader: String
    ): Call<List<Job>>
}
