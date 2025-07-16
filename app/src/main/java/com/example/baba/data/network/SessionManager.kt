package com.example.baba.data.network

object SessionManager {
    var userId: Long? = null
    var userName: String? = null
    var needsRefresh: Boolean = false

    fun setLoginInfo(userId: Long, userName: String) {
        this.userId = userId
        this.userName = userName
    }

    fun clearSession() {
        userId = null
        userName = null
        needsRefresh = false
    }
}