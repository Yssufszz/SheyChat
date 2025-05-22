package com.yusuf.sheychat.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yusuf.sheychat.R
import com.yusuf.sheychat.ui.chat.ChatActivity
import com.yusuf.sheychat.utils.Constants

object NotificationHelper {
    private const val CHANNEL_ID = "chat_notifications"
    private const val CHANNEL_NAME = "Chat Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for new chat messages"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showChatNotification(
        context: Context,
        senderName: String,
        message: String,
        isPrivateChat: Boolean,
        senderId: String = ""
    ) {
        val intent = Intent(context, ChatActivity::class.java).apply {
            if (isPrivateChat) {
                putExtra("chatType", Constants.CHAT_TYPE_PRIVATE)
                putExtra("receiverId", senderId)
                putExtra("receiverName", senderName)
            } else {
                putExtra("chatType", Constants.CHAT_TYPE_GLOBAL)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationTitle = if (isPrivateChat) senderName else "Global Chat"
        val notificationText = if (isPrivateChat) message else "$senderName: $message"

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_chat) // Pastikan ada icon ini
            .setContentTitle(notificationTitle)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .build()

        val notificationId = if (isPrivateChat) {
            senderId.hashCode()
        } else {
            1
        }

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    fun isAppInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false

        val packageName = context.packageName
        for (appProcess in appProcesses) {
            if (appProcess.importance == android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == packageName) {
                return true
            }
        }
        return false
    }
}