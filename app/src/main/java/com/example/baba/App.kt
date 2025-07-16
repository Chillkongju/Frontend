package com.example.baba

import android.app.Application
import com.example.baba.data.network.RetrofitInstance

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Retrofit 초기화 (쿠키 관리 포함)
        RetrofitInstance.init(this)
    }
}
