package com.example.baba.data.network

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object PersistentSessionManager {
    private const val PREF_NAME = "baba_session"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USERNAME = "username"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private var sharedPreferences: SharedPreferences? = null

    // 앱 시작 시 초기화
    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            loadSession()
        }
    }

    // 로그인 정보 저장
    fun saveLoginInfo(userId: Long, userName: String, username: String) {
        sharedPreferences?.edit()?.apply {
            putLong(KEY_USER_ID, userId)
            putString(KEY_USER_NAME, userName)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }

        SessionManager.setFullLoginInfo(userId, userName, username)

        Log.d("PersistentSession", "세션 저장: userId=$userId, userName=$userName, username=$username")
    }

    // 세션 정보 로드
    private fun loadSession() {
        sharedPreferences?.let { prefs ->
            if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                val userId = prefs.getLong(KEY_USER_ID, -1L)
                val userName = prefs.getString(KEY_USER_NAME, "") ?: ""
                val username = prefs.getString(KEY_USERNAME, "") ?: ""

                if (userId != -1L && userName.isNotEmpty() && username.isNotEmpty()) {
                    SessionManager.setFullLoginInfo(userId, userName, username)

                    Log.d("PersistentSession", "세션 로드: userId=$userId, userName=$userName, username=$username")
                } else {
                    Log.w("PersistentSession", "저장된 세션 정보가 불완전함")
                }
            } else {
                Log.d("PersistentSession", "저장된 로그인 상태 없음")
            }
        }
    }

    // 로그인 상태 확인
    fun isLoggedIn(): Boolean {
        val isLoggedIn = sharedPreferences?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
        Log.d("PersistentSession", "로그인 상태 확인: $isLoggedIn")
        return isLoggedIn
    }

    // 로그아웃 (세션 정보 삭제)
    fun clearSession() {
        sharedPreferences?.edit()?.apply {
            clear()
            apply()
        }

        SessionManager.clearSession()
        Log.d("PersistentSession", "세션 클리어 완료")
    }

    // 저장된 사용자 정보 조회
    fun getSavedUserInfo(): Triple<Long?, String?, String?> {
        sharedPreferences?.let { prefs ->
            if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
                val userId = prefs.getLong(KEY_USER_ID, -1L)
                val userName = prefs.getString(KEY_USER_NAME, "")
                val username = prefs.getString(KEY_USERNAME, "")

                return Triple(
                    if (userId != -1L) userId else null,
                    userName,
                    username
                )
            }
        }
        return Triple(null, null, null)
    }
}