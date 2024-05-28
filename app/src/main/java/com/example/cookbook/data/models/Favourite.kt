package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
@Entity(
    foreignKeys = [ForeignKey(
        entity = Recipe::class,
        parentColumns = ["recipeId"],
        childColumns = ["recipeId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Favourite(
    @PrimaryKey(autoGenerate = true) val favouriteId: Long = 0,
    val recipeId: Int,
    val userId: Long
)
