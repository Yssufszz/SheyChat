package com.yusuf.sheychat.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.yusuf.sheychat.MainActivity
import com.yusuf.sheychat.data.repository.UserRepository
import com.yusuf.sheychat.databinding.ActivityLoginBinding
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvResendVerification.setOnClickListener {
            resendVerificationEmail()
        }
    }

    private fun loginUser(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."

        lifecycleScope.launch {
            userRepository.loginUser(email, password)
                .onSuccess {
                    Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                }
                .onFailure { exception ->
                    val errorMessage = exception.message ?: "Login failed"
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()

                    // Show resend verification option if email not verified
                    if (errorMessage.contains("verify your email", ignoreCase = true)) {
                        binding.tvResendVerification.visibility = android.view.View.VISIBLE
                    }

                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Login"
                }
        }
    }

    private fun resendVerificationEmail() {
        lifecycleScope.launch {
            userRepository.resendVerificationEmail()
                .onSuccess { message ->
                    Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
                    binding.tvResendVerification.visibility = android.view.View.GONE
                }
                .onFailure { exception ->
                    Toast.makeText(this@LoginActivity, "Failed to resend email: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}