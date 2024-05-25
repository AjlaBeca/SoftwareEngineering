package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipes", // Ensure this matches your DAO queries
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["authorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Recipe(
    @PrimaryKey(autoGenerate = true) val recipeId: Int? = null,
    val name: String,
    val instructions: String,
    val ingredients: String,
    val time: String,
    val complexity: String,
    val servings: Int,
    val authorId: Long,
    val categoryId: Int,
    val imagePath: String?
)
