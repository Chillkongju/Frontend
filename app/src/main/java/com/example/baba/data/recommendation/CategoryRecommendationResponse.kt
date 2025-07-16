package com.example.baba.data.recommendation

import com.google.gson.annotations.SerializedName

// 카테고리별 추천 응답 DTO
data class CategoryRecommendationResponse(
    @SerializedName("category") val category: String,
    @SerializedName("recommendations") val recommendations: List<RecommendationResponse>
)