package com.example.baba.data.recommendation

import retrofit2.Response
import retrofit2.http.*

interface RecommendationApi {
    @POST("recommendations/{diary_id}")
    suspend fun recommendDaily(
        @Path("diary_id") diaryId: Long
    ): Response<List<RecommendationResponse>>

    @GET("recommendations/{diary_id}")
    suspend fun getRecommendationsByDiary(
        @Path("diary_id") diaryId: Long
    ): Response<List<RecommendationResponse>>
}