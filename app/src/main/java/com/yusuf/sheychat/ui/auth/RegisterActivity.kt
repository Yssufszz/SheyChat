package com.yusuf.sheychat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yusuf.sheychat.MainActivity
import com.yusuf.sheychat.data.repository.UserRepository
import com.yusuf.sheychat.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                registerUser(email, password, username)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser(email: String, password: String, username: String) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        lifecycleScope.launch {
            userRepository.registerUser(email, password, username)
                .onSuccess { message ->
                    // Show success dialog instead of going to MainActivity
                    showVerificationDialog(message)
                }
                .onFailure { exception ->
                    Toast.makeText(this@RegisterActivity, "Registration failed: ${exception.message}", Toast.LENGTH_LONG).show()
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Register"
                }
        }
    }

    private fun showVerificationDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Email Verification Required")
            .setMessage(message)
            .setPositiveButton("Go to Login") { _, _ ->
                finish() // Kembali ke LoginActivity
            }
            .setCancelable(false)
            .show()
    }
}