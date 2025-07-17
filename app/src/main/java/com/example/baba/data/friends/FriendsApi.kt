package com.example.baba.data.friends

import com.example.baba.data.member.MemberInfoResponse
import retrofit2.http.*

interface FriendsApi {
   @GET("friends/following")
   suspend fun getFollowingList(@Query("username") username: String): List<MemberInfoResponse>

   @GET("friends/follower")
   suspend fun getFollowerList(@Query("username") username: String): List<MemberInfoResponse >

   @POST("friends/request")
   suspend fun followUser(
      @Query("fromUsername") fromUsername: String,
      @Query("toUsername") toUsername: String
   ): retrofit2.Response<String>

   @DELETE("friends/unfollow")
   suspend fun unfollowUser(
      @Query("fromUsername") fromUsername: String,
      @Query("toUsername") toUsername: String
   ): retrofit2.Response<String>
}