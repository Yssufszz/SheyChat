package com.yusuf.sheychat.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.database.FirebaseDatabase

object FirebaseHelper {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    val database: FirebaseDatabase by lazy { FirebaseDatabase.getInstance() }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun generateUserCode(): String {
        return (100000..999999).random().toString()
    }

    fun generateMessageId(): String {
        return firestore.collection("temp").document().id
    }
}