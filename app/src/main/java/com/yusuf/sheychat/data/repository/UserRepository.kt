package com.yusuf.sheychat.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yusuf.sheychat.data.model.User
import com.yusuf.sheychat.utils.Constants
import com.yusuf.sheychat.utils.FirebaseHelper
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseHelper.auth
    private val firestore = FirebaseHelper.firestore

    suspend fun registerUser(email: String, password: String, username: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")

            firebaseUser.sendEmailVerification().await()

            val userCode = FirebaseHelper.generateUserCode()
            val user = User(
                uid = firebaseUser.uid,
                username = username,
                email = email,
                userCode = userCode,
                isOnline = false
            )

            firestore.collection(Constants.COLLECTION_USERS)
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()

            auth.signOut()

            Result.success("Registrasi berhasil! Cek email kamu ya, terus klik link verifikasinya sebelum login.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Login failed")

            firebaseUser.reload().await()
            if (!firebaseUser.isEmailVerified) {
                auth.signOut()
                throw Exception("Tolong verifikasi email kamu dulu ya. Cek inbox buat link verifikasinya.")
            }

            val userId = firebaseUser.uid

            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("isOnline", true, "lastSeen", System.currentTimeMillis())
                .await()

            val userDoc = firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .await()

            val user = userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resendVerificationEmail(): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null && !currentUser.isEmailVerified) {
                currentUser.sendEmailVerification().await()
                Result.success("Email verifikasi sudah dikirim! Cek inbox kamu ya.")
            } else {
                Result.failure(Exception("User gak ketemu atau email-nya udah pernah diverifikasi sebelumnya."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByCode(userCode: String): Result<User> {
        return try {
            val querySnapshot = firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo("userCode", userCode)
                .get()
                .await()

            if (querySnapshot.documents.isNotEmpty()) {
                val user = querySnapshot.documents[0].toObject(User::class.java)
                    ?: throw Exception("User data not found")
                Result.success(user)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserOnlineStatus(isOnline: Boolean) {
        try {
            val userId = FirebaseHelper.getCurrentUserId() ?: return
            firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update(
                    "isOnline", isOnline,
                    "lastSeen", System.currentTimeMillis()
                )
                .await()
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun signOut() {
        auth.signOut()
    }
}