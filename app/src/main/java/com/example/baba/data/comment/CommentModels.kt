package com.example.baba.data.comment

import com.google.gson.annotations.SerializedName

// 댓글 응답 DTO
data class CommentResponseDto(
    @SerializedName("id") val id: Long,
    @SerializedName("content") val content: String,
    @SerializedName("username") val username: String,
    @SerializedName("createdAt") val createdAt: String
)

// 댓글 작성 요청
data class CommentCreateRequest(
    val username: String,
    val diaryId: Long,
    val content: String
)

// 댓글 삭제 요청
data class CommentDeleteRequest(
    val username: String,
    val commentId: Long
)