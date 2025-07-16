package com.example.baba.data.friends

import retrofit2.http.*

interface FriendsApi {
   @GET("friends/following")
   suspend fun getFollowingList(@Query("username") username: String): List<FriendResponse>

   @GET("friends/follower")
   suspend fun getFollowerList(@Query("username") username: String): List<FriendResponse>
}