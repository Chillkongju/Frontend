package com.example.baba.data.auth

import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // 로그인
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<String>

    // 회원가입
    @POST("auth/signup")
    suspend fun signup(
        @Query("username") username: String,
        @Query("name") name: String,
        @Query("password") password: String,
        @Query("confirmPassword") confirmPassword: String
    ): Response<String>

    // 아이디 중복 확인
    @GET("auth/check-username")
    suspend fun checkUsername(
        @Query("username") username: String
    ): Response<Boolean>

    // 로그아웃
    @POST("auth/logout")
    suspend fun logout(
        @Header("authorization") accessToken: String,
        @Header("refresh-token") refreshToken: String
    ): Response<String>
}
