package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(entity = Recipe::class, parentColumns = ["recipeId"], childColumns = ["recipeId"]),
        ForeignKey(entity = User::class, parentColumns = ["userId"], childColumns = ["userId"])
    ]
)
data class Favourite(
    @PrimaryKey(autoGenerate = true) val favouriteId: Long = 0,
    val recipeId: Int,
    val userId: Long
)