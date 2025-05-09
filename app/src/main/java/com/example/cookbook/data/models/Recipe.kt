package com.example.cookbook.data.models
import com.google.firebase.firestore.DocumentId

data class Recipe(
    @DocumentId val documentId: String = "", // Firestore document ID
    val recipeId: Int? = null,
    val name: String = "",
    val instructions: String = "",
    val ingredients: String = "",
    val time: String = "",
    val complexity: String = "",
    val servings: Int = 0,
    val authorId: Long = 0,
    val categoryId: Int = 0,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)