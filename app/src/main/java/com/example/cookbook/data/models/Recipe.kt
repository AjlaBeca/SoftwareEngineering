package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Int? = null,
    val name: String,
    val instructions: String,
    val ingredients: String,
    val time: String,
    val complexity: String,
    val servings: Int,
    val authorId: Int,
    val categoryId: Int,
    val imagePath: String?
)