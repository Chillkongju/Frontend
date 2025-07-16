package com.example.baba.data.love

import com.google.gson.annotations.SerializedName

// 좋아요 토글 응답
data class LoveToggleResponse(
    @SerializedName("liked") val liked: Boolean,
    @SerializedName("message") val message: String
)

// 좋아요 확인 응답
data class LoveCheckResponse(
    @SerializedName("liked") val liked: Boolean
)