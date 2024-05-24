package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_recipes",
    foreignKeys = [
        ForeignKey(entity = User::class, parentColumns = ["userId"], childColumns = ["userId"]),
        ForeignKey(entity = Recipe::class, parentColumns = ["recipeId"], childColumns = ["recipeId"])
    ]
)
data class UserRecipe(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val recipeId: Long
)
