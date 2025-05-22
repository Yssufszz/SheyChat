package com.yusuf.sheychat.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.QuerySnapshot
import com.yusuf.sheychat.data.model.Message
import com.yusuf.sheychat.data.model.User
import com.yusuf.sheychat.data.repository.ChatRepository
import com.yusuf.sheychat.databinding.ActivityChatBinding
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper
import com.yusuf.sheychat.utils.NotificationHelper
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatRepository = ChatRepository()

    private var chatType: String = Constants.CHAT_TYPE_GLOBAL
    private var receiverId: String = ""
    private var receiverName: String = ""
    private var currentUser: User? = null
    private var isActivityVisible = false
    private var lastMessageCount = 0

    // Permission launcher untuk notifikasi (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get intent data
        chatType = intent.getStringExtra("chatType") ?: Constants.CHAT_TYPE_GLOBAL
        receiverId = intent.getStringExtra("receiverId") ?: ""
        receiverName = intent.getStringExtra("receiverName") ?: ""

        setupNotifications()
        setupUI()
        setupRecyclerView()
        setupClickListeners()
        loadMessages()
        getCurrentUser()
    }

    override fun onResume() {
        super.onResume()
        isActivityVisible = true
    }

    override fun onPause() {
        super.onPause()
        isActivityVisible = false
    }

    private fun setupNotifications() {
        // Create notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request notification permission untuk Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupUI() {
        supportActionBar?.title = when (chatType) {
            Constants.CHAT_TYPE_GLOBAL -> "Global Chat"
            Constants.CHAT_TYPE_PRIVATE -> "Chat with $receiverName"
            else -> "Chat"
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter()
        binding.recyclerViewMessages.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity)
        }
    }

    private fun setupClickListeners() {
        binding.btnSend.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.etMessage.text?.clear()
            }
        }
    }

    private fun getCurrentUser() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        FirebaseHelper.firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                currentUser = document.toObject(User::class.java)
            }
    }

    private fun sendMessage(message: String) {
        val senderName = currentUser?.username ?: "Unknown"

        lifecycleScope.launch {
            val result = when (chatType) {
                Constants.CHAT_TYPE_GLOBAL -> {
                    chatRepository.sendGlobalMessage(message, senderName)
                }
                Constants.CHAT_TYPE_PRIVATE -> {
                    chatRepository.sendPrivateMessage(message, senderName, receiverId)
                }
                else -> Result.failure(Exception("Invalid chat type"))
            }

            result.onFailure { exception ->
                Toast.makeText(this@ChatActivity, "Failed to send message: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadMessages() {
        val query = when (chatType) {
            Constants.CHAT_TYPE_GLOBAL -> chatRepository.getGlobalMessagesQuery()
            Constants.CHAT_TYPE_PRIVATE -> chatRepository.getPrivateMessagesQuery(receiverId)
            else -> return
        }

        query.addSnapshotListener(EventListener<QuerySnapshot> { snapshots, error ->
            if (error != null) {
                Toast.makeText(this, "Error loading messages: ${error.message}", Toast.LENGTH_SHORT).show()
                return@EventListener
            }

            if (snapshots != null) {
                val messages = snapshots.documents.mapNotNull { document ->
                    document.toObject(Message::class.java)
                }

                // Check for new messages dan tampilkan notifikasi
                checkForNewMessages(messages)

                chatAdapter.updateMessages(messages)

                // Scroll to bottom
                if (messages.isNotEmpty()) {
                    binding.recyclerViewMessages.scrollToPosition(messages.size - 1)
                }
            }
        })
    }

    private fun checkForNewMessages(messages: List<Message>) {
        // Skip jika belum ada pesan sebelumnya (first load)
        if (lastMessageCount == 0) {
            lastMessageCount = messages.size
            return
        }

        // Ada pesan baru
        if (messages.size > lastMessageCount) {
            val newMessages = messages.drop(lastMessageCount)
            lastMessageCount = messages.size

            // Tampilkan notifikasi hanya jika:
            // 1. Activity tidak visible (app di background)
            // 2. Pesan bukan dari user sendiri
            if (!isActivityVisible) {
                for (newMessage in newMessages) {
                    val currentUserId = FirebaseHelper.getCurrentUserId()

                    // Skip jika pesan dari user sendiri
                    if (newMessage.senderId == currentUserId) continue

                    // Tampilkan notifikasi
                    NotificationHelper.showChatNotification(
                        context = this,
                        senderName = newMessage.senderName,
                        message = newMessage.message,
                        isPrivateChat = chatType == Constants.CHAT_TYPE_PRIVATE,
                        senderId = newMessage.senderId
                    )
                }
            }
        }
    }
}