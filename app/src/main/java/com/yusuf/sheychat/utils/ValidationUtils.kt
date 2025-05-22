package com.yusuf.sheychat.utils

import java.util.regex.Pattern

object ValidationUtils {

    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                "\\@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(" +
                "\\." +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                ")+"
    )

    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    fun isValidUsername(username: String): Boolean {
        return username.length >= 3 && username.matches(Regex("^[a-zA-Z0-9_]+$"))
    }

    fun isValidUserCode(userCode: String): Boolean {
        return userCode.length == 6 && userCode.matches(Regex("^[0-9]+$"))
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = ""
    )

    fun validateRegistration(username: String, email: String, password: String): ValidationResult {
        return when {
            username.isEmpty() -> ValidationResult(false, "Username is required")
            !isValidUsername(username) -> ValidationResult(false, "Username must be at least 3 characters and contain only letters, numbers, and underscores")
            email.isEmpty() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Please enter a valid email address")
            password.isEmpty() -> ValidationResult(false, "Password is required")
            !isValidPassword(password) -> ValidationResult(false, "Password must be at least 6 characters")
            else -> ValidationResult(true)
        }
    }

    fun validateLogin(email: String, password: String): ValidationResult {
        return when {
            email.isEmpty() -> ValidationResult(false, "Email is required")
            !isValidEmail(email) -> ValidationResult(false, "Please enter a valid email address")
            password.isEmpty() -> ValidationResult(false, "Password is required")
            else -> ValidationResult(true)
        }
    }
}