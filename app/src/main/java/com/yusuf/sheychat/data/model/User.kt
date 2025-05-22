package com.yusuf.sheychat.data.model

data class User(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val userCode: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "username" to username,
            "email" to email,
            "profileImageUrl" to profileImageUrl,
            "isOnline" to isOnline,
            "lastSeen" to lastSeen,
            "userCode" to userCode
        )
    }
}