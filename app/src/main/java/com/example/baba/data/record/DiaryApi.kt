package com.example.baba.data.record

import retrofit2.Response
import retrofit2.http.*

interface DiaryApi {
    @POST("diaries")
    suspend fun createDiary(
        @Query("id") userId: Long,
        @Body diaryCreateRequest: DiaryCreateRequest
    ): Response<Void>

    @GET("diaries/me")
    suspend fun getAllMyDiaries(@Query("id") userId: Long): Response<List<DiaryResponse>>
}