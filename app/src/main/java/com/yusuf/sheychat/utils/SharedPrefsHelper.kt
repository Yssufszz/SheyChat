package com.yusuf.sheychat.utils

import android.content.Context
import android.content.SharedPreferences

class SharedPrefsHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "sheychat_prefs"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_USER_CODE = "user_code"
        private const val KEY_EMAIL = "email"
        private const val KEY_IS_FIRST_TIME = "is_first_time"
    }

    fun saveUserData(userId: String, username: String, userCode: String, email: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_USER_CODE, userCode)
            putString(KEY_EMAIL, email)
            apply()
        }
    }

    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun getUsername(): String? = prefs.getString(KEY_USERNAME, null)
    fun getUserCode(): String? = prefs.getString(KEY_USER_CODE, null)
    fun getEmail(): String? = prefs.getString(KEY_EMAIL, null)

    fun isFirstTime(): Boolean = prefs.getBoolean(KEY_IS_FIRST_TIME, true)

    fun setFirstTime(isFirstTime: Boolean) {
        prefs.edit().putBoolean(KEY_IS_FIRST_TIME, isFirstTime).apply()
    }

    fun clearUserData() {
        prefs.edit().clear().apply()
    }
}