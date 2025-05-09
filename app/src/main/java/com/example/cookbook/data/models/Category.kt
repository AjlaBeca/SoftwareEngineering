package com.example.cookbook.data.models

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId val documentId: String = "", // Firestore document ID
    val categoryId: Int = 0,
    val name: String = ""
)