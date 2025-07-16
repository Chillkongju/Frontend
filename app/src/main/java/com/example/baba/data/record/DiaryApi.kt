package com.example.baba.data.record

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface DiaryApi {
    @Multipart
    @POST("diaries")
    suspend fun createDiary(
        @Part("id") userId: Long,
        @Part("title") title: String,
        @Part("content") content: String,
        @Part("category") category: String,
        @Part("rating") rating: Double,
        @Part("watchedAt") watchedAt: String,
        @Part imageFile: MultipartBody.Part? = null
    ): Response<DiaryResponse>

    @GET("diaries/me")
    suspend fun getAllMyDiaries(@Query("id") userId: Long): Response<List<DiaryResponse>>
}