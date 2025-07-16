package com.example.baba.data.love

import retrofit2.Response
import retrofit2.http.*

interface LoveApi {
    // 좋아요 토글
    @POST("love/toggle")
    suspend fun toggleLove(
        @Query("username") username: String,
        @Query("diaryId") diaryId: Long
    ): Response<String>

    // 좋아요 여부 확인
    @GET("love/check")
    suspend fun checkLove(
        @Query("username") username: String,
        @Query("diaryId") diaryId: Long
    ): Response<Boolean>
}