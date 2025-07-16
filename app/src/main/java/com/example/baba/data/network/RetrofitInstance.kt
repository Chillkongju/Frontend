package com.example.baba.data.network

import android.content.Context
import android.content.SharedPreferences
import com.example.baba.data.auth.AuthApi
import com.example.baba.data.comment.CommentApi
import com.example.baba.data.friends.FriendsApi
import com.example.baba.data.love.LoveApi
import com.example.baba.data.member.MemberApi
import com.example.baba.data.recommendation.RecommendationApi
import com.example.baba.data.record.DiaryApi
import com.google.gson.GsonBuilder
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.JavaNetCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private var client: OkHttpClient? = null

    fun init(context: Context) {
        if (client == null) {
            val cookieManager = CookieManager()
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL)

            client = OkHttpClient.Builder()
                .cookieJar(JavaNetCookieJar(cookieManager))
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
    }

    private val gson = GsonBuilder().create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/api/v1/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client ?: OkHttpClient())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }
    val memberApi: MemberApi by lazy { retrofit.create(MemberApi::class.java) }
    val diaryApi: DiaryApi by lazy { retrofit.create(DiaryApi::class.java) }
    val friendsApi: FriendsApi by lazy { retrofit.create(FriendsApi::class.java) }
    val recommendationApi: RecommendationApi by lazy { retrofit.create(RecommendationApi::class.java) }
    val loveApi: LoveApi by lazy { retrofit.create(LoveApi::class.java) }
    val commentApi: CommentApi by lazy { retrofit.create(CommentApi::class.java) }
}
