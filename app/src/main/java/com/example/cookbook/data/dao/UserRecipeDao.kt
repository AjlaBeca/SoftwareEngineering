package com.example.cookbook.data.dao
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.cookbook.data.models.UserRecipe

@Dao
interface UserRecipeDao {
    @Transaction
    @Query("SELECT * FROM user_recipes")
    fun getUsersWithRecipes(): List<UserRecipe>
}
