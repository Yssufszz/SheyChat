package com.yusuf.sheychat.services

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yusuf.sheychat.utils.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM Token", "New token: $token")

        // TODO: Kirim token ke server untuk push notification
        // sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "From: ${remoteMessage.from}")

        // Handle data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")

            val senderName = remoteMessage.data["senderName"] ?: "Unknown"
            val message = remoteMessage.data["message"] ?: ""
            val isPrivateChat = remoteMessage.data["isPrivateChat"]?.toBoolean() ?: false
            val senderId = remoteMessage.data["senderId"] ?: ""

            // Tampilkan notifikasi
            NotificationHelper.showChatNotification(
                context = this,
                senderName = senderName,
                message = message,
                isPrivateChat = isPrivateChat,
                senderId = senderId
            )
        }

        // Handle notification payload (jika ada)
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
        }
    }
}