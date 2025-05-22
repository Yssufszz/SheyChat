package com.yusuf.sheychat.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yusuf.sheychat.data.model.User
import com.yusuf.sheychat.databinding.ActivityProfileBinding
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadUserProfile()
    }

    private fun loadUserProfile() {
        val userId = FirebaseHelper.getCurrentUserId() ?: return

        FirebaseHelper.firestore.collection(Constants.COLLECTION_USERS)
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                if (user != null) {
                    displayUserInfo(user)
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun displayUserInfo(user: User) {
        binding.tvUsername.text = user.username
        binding.tvEmail.text = user.email
        binding.tvUserCode.text = "Your Code: ${user.userCode}"
        binding.tvUserId.text = "User ID: ${user.uid}"
    }
}