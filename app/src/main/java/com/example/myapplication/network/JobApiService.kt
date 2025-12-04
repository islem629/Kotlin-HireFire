package com.example.myapplication.network

import com.example.myapplication.model.Job
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface JobApiService {
    @GET("jobs/recommendations")
    fun getRecommendedJobs(
        @Header("Authorization") authHeader: String
    ): Call<List<Job>>
    @POST("jobs/{jobId}/apply")
    suspend fun applyToJob(
        @Path("jobId") jobId: Long,
        @Header("Authorization") authHeader: String
    ): Response<Void>
}
