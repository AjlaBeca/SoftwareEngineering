package com.example.cookbook.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookbook.data.models.CategoryRecipe

@Dao
interface CategoryRecipeDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCategoryRecipe(categoryRecipe: CategoryRecipe)

    @Query("SELECT * FROM category_recipes ORDER BY categoryId, recipeId ASC")
    fun readAllData(): LiveData<List<CategoryRecipe>>
}
