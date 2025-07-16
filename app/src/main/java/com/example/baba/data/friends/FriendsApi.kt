package com.example.baba.data.friends

import retrofit2.http.*

interface FriendsApi {
   @GET("friends/following")
   suspend fun getFollowingList(): List<FriendResponse>

   @GET("friends/follower")
   suspend fun getFollowerList(): List<FriendResponse>
}