package com.yusuf.sheychat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yusuf.sheychat.data.repository.UserRepository
import com.yusuf.sheychat.databinding.ActivityMainBinding
import com.yusuf.sheychat.ui.auth.LoginActivity
import com.yusuf.sheychat.ui.chat.ChatActivity
import com.yusuf.sheychat.ui.profile.ProfileActivity
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if user is logged in
        if (FirebaseHelper.getCurrentUserId() == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardGlobalChat.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatType", Constants.CHAT_TYPE_GLOBAL)
            startActivity(intent)
        }

        binding.cardPrivateChat.setOnClickListener {
            showPrivateChatDialog()
        }

        binding.cardProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                userRepository.updateUserOnlineStatus(false)
                userRepository.signOut()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun showPrivateChatDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Mulai Chat Private")

        val input = android.widget.EditText(this)
        input.hint = "Masukin 6 digit id code teman anda"
        builder.setView(input)

        builder.setPositiveButton("Start Chat") { _, _ ->
            val userCode = input.text.toString().trim()
            if (userCode.isNotEmpty()) {
                findAndStartPrivateChat(userCode)
            } else {
                Toast.makeText(this, "Please enter a user code", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun findAndStartPrivateChat(userCode: String) {
        lifecycleScope.launch {
            userRepository.getUserByCode(userCode)
                .onSuccess { user ->
                    val intent = Intent(this@MainActivity, ChatActivity::class.java)
                    intent.putExtra("chatType", Constants.CHAT_TYPE_PRIVATE)
                    intent.putExtra("receiverId", user.uid)
                    intent.putExtra("receiverName", user.username)
                    startActivity(intent)
                }
                .onFailure {
                    Toast.makeText(this@MainActivity, "User not found", Toast.LENGTH_SHORT).show()
                }
        }
    }
}