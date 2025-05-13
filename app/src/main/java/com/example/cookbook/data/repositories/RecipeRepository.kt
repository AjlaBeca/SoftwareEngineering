package com.example.cookbook.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.cookbook.data.AppDatabase
import com.example.cookbook.data.dao.RecipeDao
import com.example.cookbook.data.models.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository(private val recipeDao: RecipeDao) {
    val readAllData: LiveData<List<Recipe>> = recipeDao.readAllData()

    suspend fun addRecipe(recipe: Recipe) {
        recipeDao.addRecipe(recipe)
    }

    suspend fun deleteRecipe(recipeId: Int) {
        recipeDao.deleteRecipe(recipeId)
    }

    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> {
        return recipeDao.readRecipesByCategory(categoryId)
    }

    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>> {
        return recipeDao.getRecipesByUser(userId)
    }
}
