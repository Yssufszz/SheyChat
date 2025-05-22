package com.yusuf.sheychat.data.model

data class Message(
    val messageId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val chatType: String = "private",
    val receiverId: String = ""
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "messageId" to messageId,
            "senderId" to senderId,
            "senderName" to senderName,
            "message" to message,
            "timestamp" to timestamp,
            "chatType" to chatType,
            "receiverId" to receiverId
        )
    }
}