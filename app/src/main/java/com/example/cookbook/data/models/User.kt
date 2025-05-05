// Update User.kt
package com.example.cookbook.data.models

import com.google.firebase.firestore.DocumentId

data class User(
    @DocumentId val documentId: String = "", // Firestore document ID
    val userId: Long = 0,
    var email: String = "",
    var password: String = "", // Note: Only stored temporarily
    var username: String = "",
    val image: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)