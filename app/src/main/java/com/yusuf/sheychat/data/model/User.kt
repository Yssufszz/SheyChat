package com.yusuf.sheychat.data.model

data class User(
    val uid: String = "",
    var username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    var isOnline: Boolean = false,
    var lastSeen: Long = System.currentTimeMillis(),
    val userCode: String = "",
    var lastUsernameChange: Long = 0
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "isOnline" to isOnline,
            "lastSeen" to lastSeen,
            "userCode" to userCode,
            "lastUsernameChange" to lastUsernameChange
        )
    }
}