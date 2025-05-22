package com.yusuf.sheychat.ui.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.sheychat.data.model.User
import com.yusuf.sheychat.databinding.ActivityProfileBinding
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper
import java.util.concurrent.TimeUnit

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private var currentUser: User? = null

    // Konstanta untuk batasan waktu (7 hari dalam milliseconds)
    private val USERNAME_UPDATE_COOLDOWN = TimeUnit.DAYS.toMillis(7)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserProfile()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Copy button click listener
        binding.ivCopyCode.setOnClickListener {
            copyUserCodeToClipboard()
        }

        // Share button click listener
        binding.btnShareProfile.setOnClickListener {
            shareProfileCode()
        }

        // Username click listener untuk edit
        binding.tvUsername.setOnClickListener {
            showEditUsernameDialog()
        }

        // Tambahkan visual feedback bahwa username bisa diklik
        binding.tvUsername.isClickable = true
        binding.tvUsername.isFocusable = true
    }

    private fun loadUserProfile() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        FirebaseHelper.firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    currentUser = user
                    displayUserInfo(user)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayUserInfo(user: User) {
        binding.tvUsername.text = user.username
        binding.tvEmail.text = user.email
        binding.tvUserCode.text = user.userCode
        binding.tvUserId.text = user.uid
    }

    private fun showEditUsernameDialog() {
        currentUser?.let { user ->
            // Cek apakah user bisa mengubah username
            val canChangeUsername = canChangeUsername(user.lastUsernameChange ?: 0)

            if (!canChangeUsername) {
                val remainingTime = getRemainingCooldownTime(user.lastUsernameChange ?: 0)
                showCooldownMessage(remainingTime)
                return
            }

            // Buat dialog untuk edit username
            val editText = EditText(this).apply {
                setText(user.username)
                inputType = InputType.TYPE_CLASS_TEXT
                hint = "Masukkan username baru"
                setPadding(50, 40, 50, 40)
            }

            AlertDialog.Builder(this)
                .setTitle("Edit Username")
                .setMessage("Kamu hanya bisa mengubah username seminggu sekali. Yakin mau mengubah?")
                .setView(editText)
                .setPositiveButton("Simpan") { dialog, _ ->
                    val newUsername = editText.text.toString().trim()
                    if (newUsername.isNotEmpty() && newUsername != user.username) {
                        updateUsername(newUsername)
                    } else if (newUsername.isEmpty()) {
                        Toast.makeText(this, "Username tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Batal") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun canChangeUsername(lastChangeTime: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastChangeTime) >= USERNAME_UPDATE_COOLDOWN
    }

    private fun getRemainingCooldownTime(lastChangeTime: Long): Long {
        val currentTime = System.currentTimeMillis()
        val timePassed = currentTime - lastChangeTime
        return USERNAME_UPDATE_COOLDOWN - timePassed
    }

    private fun showCooldownMessage(remainingTime: Long) {
        val days = TimeUnit.MILLISECONDS.toDays(remainingTime)
        val hours = TimeUnit.MILLISECONDS.toHours(remainingTime) % 24

        val message = if (days > 0) {
            "Kamu bisa mengubah username lagi dalam ${days} hari ${hours} jam"
        } else {
            "Kamu bisa mengubah username lagi dalam ${hours} jam"
        }

        AlertDialog.Builder(this)
            .setTitle("Username Baru Saja Diubah")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateUsername(newUsername: String) {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        // Tampilkan loading
        Toast.makeText(this, "Mengupdate username...", Toast.LENGTH_SHORT).show()

        // Update username di Firestore
        val updateData = hashMapOf<String, Any>(
            "username" to newUsername,
            "lastUsernameChange" to System.currentTimeMillis()
        )

        FirebaseHelper.firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .update(updateData)
            .addOnSuccessListener {
                // Update local user object
                currentUser?.let { user ->
                    user.username = newUsername
                    user.lastUsernameChange = System.currentTimeMillis()
                    displayUserInfo(user)
                }

                Toast.makeText(this, "Username berhasil diubah!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengubah username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun copyUserCodeToClipboard() {
        currentUser?.let { user ->
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("User Code", user.userCode)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, "User code copied to clipboard!", Toast.LENGTH_SHORT).show()
        } ?: run {
            Toast.makeText(this, "User code not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareProfileCode() {
        currentUser?.let { user ->
            val shareText = buildString {
                append("Join me on SheyChat!\n\n")
                append("My Profile:\n")
                append("Username: ${user.username}\n")
                append("User Code: ${user.userCode}\n\n")
                append("Use my code to connect with me!")
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "SheyChat Profile - ${user.username}")
            }

            startActivity(Intent.createChooser(shareIntent, "Share Profile Code"))
        } ?: run {
            Toast.makeText(this, "Profile not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }
}