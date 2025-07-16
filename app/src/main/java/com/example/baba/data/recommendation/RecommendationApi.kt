package com.example.baba.data.recommendation

import retrofit2.Response
import retrofit2.http.*

interface RecommendationApi {
    // 일일 추천
    @POST("recommendations/{diary_id}")
    suspend fun recommendDaily(
        @Path("diary_id") diaryId: Long
    ): Response<List<RecommendationResponse>>

    @GET("recommendations/{diary_id}")
    suspend fun getRecommendationsByDiary(
        @Path("diary_id") diaryId: Long
    ): Response<List<RecommendationResponse>>

    // 월별 추천
    @POST("recommendations/monthly/{memberId}")
    suspend fun recommendNextMonth(
        @Path("memberId") memberId: Long
    ): Response<List<CategoryRecommendationResponse>>

    @GET("recommendations/monthly")
    suspend fun getMonthlyRecommendations(
        @Query("memberId") memberId: Long
    ): Response<List<CategoryRecommendationResponse>>

    @GET("recommendations/member/{memberId}/{recommendationId}")
    suspend fun getRecommendation(
        @Path("memberId") memberId: Long,
        @Path("recommendationId") recommendationId: Long
    ): Response<RecommendationResponse>
}