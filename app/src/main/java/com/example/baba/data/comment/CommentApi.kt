package com.example.baba.data.comment

import retrofit2.Response
import retrofit2.http.*

interface CommentApi {
    // 댓글 작성
    @POST("comment")
    suspend fun createComment(
        @Query("username") username: String,
        @Query("diaryId") diaryId: Long,
        @Query("content") content: String
    ): Response<Void>

    // 댓글 삭제
    @DELETE("comment")
    suspend fun deleteComment(
        @Query("username") username: String,
        @Query("commentId") commentId: Long
    ): Response<Void>

    // 댓글 목록 조회
    @GET("comment")
    suspend fun getComments(
        @Query("diaryId") diaryId: Long
    ): Response<List<CommentResponseDto>>
}