package com.example.baba.data.network

object SessionManager {
    var userId: Long? = null
    var userName: String? = null
    var username: String? = null  // 실제 로그인 ID
    var needsRefresh: Boolean = false

    fun setLoginInfo(userId: Long, userName: String) {
        this.userId = userId
        this.userName = userName
    }

    fun setFullLoginInfo(userId: Long, userName: String, username: String) {
        this.userId = userId
        this.userName = userName
        this.username = username
    }

    fun clearSession() {
        userId = null
        userName = null
        username = null
        needsRefresh = false
    }
}