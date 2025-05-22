package com.yusuf.sheychat.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yusuf.sheychat.data.model.Message
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseHelper.firestore

    suspend fun sendGlobalMessage(message: String, senderName: String): Result<Unit> {
        return try {
            val messageId = FirebaseHelper.generateMessageId()
            val senderId = FirebaseHelper.getCurrentUserId() ?: throw Exception("User not logged in")

            val messageObj = Message(
                messageId = messageId,
                senderId = senderId,
                senderName = senderName,
                message = message,
                chatType = Constants.CHAT_TYPE_GLOBAL
            )

            firestore.collection(Constants.COLLECTION_GLOBAL_CHAT)
                .document(messageId)
                .set(messageObj.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPrivateMessage(
        message: String,
        senderName: String,
        receiverId: String
    ): Result<Unit> {
        return try {
            val messageId = FirebaseHelper.generateMessageId()
            val senderId = FirebaseHelper.getCurrentUserId() ?: throw Exception("User not logged in")

            val messageObj = Message(
                messageId = messageId,
                senderId = senderId,
                senderName = senderName,
                message = message,
                chatType = Constants.CHAT_TYPE_PRIVATE,
                receiverId = receiverId
            )

            val chatRoomId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"

            firestore.collection(Constants.COLLECTION_PRIVATE_CHATS)
                .document(chatRoomId)
                .collection("messages")
                .document(messageId)
                .set(messageObj.toMap())
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getGlobalMessagesQuery(): Query {
        return firestore.collection(Constants.COLLECTION_GLOBAL_CHAT)
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }

    fun getPrivateMessagesQuery(receiverId: String): Query {
        val senderId = FirebaseHelper.getCurrentUserId() ?: throw Exception("User not logged in")
        val chatRoomId = if (senderId < receiverId) "${senderId}_$receiverId" else "${receiverId}_$senderId"

        return firestore.collection(Constants.COLLECTION_PRIVATE_CHATS)
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
    }
}