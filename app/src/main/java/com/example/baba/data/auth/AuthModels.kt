package com.example.baba.data.auth

// 로그인 요청
data class LoginRequest(
    val username: String,
    val password: String
)

// 로그인 응답
data class LoginResponse(
    val success: Boolean,
    val message: String
)


// 회원가입 요청
data class SignupRequest(
    val username: String,
    val password: String,
    val passwordCheck: String,
    val name: String,
    val email: String
)

data class BaseResponse(
    val status: String,
    val message: String
)

// 아이디 중복 확인
data class UserIdCheckResponse(
    val status: String,
    val message: String,
    val available: Boolean
)

