package com.example.baba.data.network

import com.example.baba.data.auth.AuthApi
import com.example.baba.data.record.DiaryApi
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitInstance {
    private val gson = GsonBuilder()
        .create()

    // 공용 Retrofit 인스턴스
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/api/v1/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val diaryApi: DiaryApi by lazy {
        retrofit.create(DiaryApi::class.java)
    }
}