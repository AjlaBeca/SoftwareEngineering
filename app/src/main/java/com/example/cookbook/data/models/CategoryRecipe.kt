package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "category_recipes",
    primaryKeys = ["categoryId", "recipeId"],
    foreignKeys = [
        ForeignKey(entity = Category::class, parentColumns = ["categoryId"], childColumns = ["categoryId"]),
        ForeignKey(entity = Recipe::class, parentColumns = ["recipeId"], childColumns = ["recipeId"])
    ]
)
data class CategoryRecipe(
    val categoryId: Long,
    val recipeId: Long
)
