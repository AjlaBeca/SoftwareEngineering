package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey val categoryId: Int,
    val name: String
)

enum class CategoryType(val id: Int, val displayName: String) {
    BREAKFAST(1, "Breakfast"),
    LUNCH(2, "Lunch"),
    DINNER(3, "Dinner"),
    DESSERT(4, "Dessert");

    companion object {
        fun fromId(id: Int): CategoryType? = values().find { it.id == id }
    }
}
