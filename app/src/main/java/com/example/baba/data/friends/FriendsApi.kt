package com.example.baba.data.friends

import retrofit2.Response
import retrofit2.http.*

interface FriendsApi {
   @GET("friends/following")
   suspend fun getFollowingList(@Query("username") username: String): List<String>

   @GET("friends/follower")
   suspend fun getFollowerList(@Query("username") username: String): List<String>

   @POST("friends/request")
   suspend fun followUser(
      @Query("fromUsername") fromUsername: String,
      @Query("toUsername") toUsername: String
   ): Response<String>

   @DELETE("friends/unfollow")
   suspend fun unfollowUser(
      @Query("fromUsername") fromUsername: String,
      @Query("toUsername") toUsername: String
   ): Response<String>
}