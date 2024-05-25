package com.example.cookbook.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookbook.data.models.Recipe

@Dao
interface RecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addRecipe(recipe: Recipe)

    @Query("SELECT * FROM recipes ORDER BY recipeId DESC") // Match the table name
    fun readAllData(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE categoryId = :categoryId ORDER BY recipeId DESC") // Match the table name
    fun readRecipesByCategory(categoryId: Int): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE authorId = :userId")
    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>>

}