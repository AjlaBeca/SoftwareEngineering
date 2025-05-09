package com.example.cookbook.data.models
import com.google.firebase.firestore.DocumentId

data class Favourite(
    @DocumentId val documentId: String = "", // Firestore document ID
    val favouriteId: Long = 0,
    val recipeId: Int = 0,
    val userId: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)