package com.example.baba.data.member

import retrofit2.Response
import retrofit2.http.*

interface MemberApi {
    // 내 정보 조회
    @GET("members/me")
    suspend fun getMyInfo(): Response<MemberInfoResponse>

    // 프로필 수정
    @PATCH("members/me")
    suspend fun updateMyInfo(
        @Body updateProfileRequest: UpdateProfileRequest
    ): Response<String>
}
