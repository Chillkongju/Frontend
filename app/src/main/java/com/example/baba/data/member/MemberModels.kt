package com.example.baba.data.member

import com.google.gson.annotations.SerializedName

// 회원 정보 조회 응답 DTO
data class MemberInfoResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("username") val username: String,
    @SerializedName("name") val name: String,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("preference") val preference: String?,
    @SerializedName("link") val link: String?
)

// 프로필 업데이트 요청 DTO
data class UpdateProfileRequest(
    @SerializedName("name") val name: String?,
    @SerializedName("profileImageUrl") val profileImageUrl: String?,
    @SerializedName("bio") val bio: String?,
    @SerializedName("preference") val preference: String?,
    @SerializedName("link") val link: String?
)