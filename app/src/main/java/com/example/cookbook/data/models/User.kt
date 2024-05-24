package com.example.cookbook.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    var email: String,
    var password: String,
    var username: String,
    val image: String?
)