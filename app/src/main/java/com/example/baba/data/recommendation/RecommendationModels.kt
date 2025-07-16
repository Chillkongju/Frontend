package com.example.baba.data.recommendation

import com.google.gson.annotations.SerializedName

data class RecommendationResponse(
    @SerializedName("title") val title: String,
    @SerializedName("releaseDate") val releaseDate: String,
    @SerializedName("genre") val genre: String,
    @SerializedName("summary") val summary: String
)