package com.example.cookbook.data.repositories

import androidx.lifecycle.LiveData
import com.example.cookbook.data.dao.CategoryDao
import com.example.cookbook.data.dao.RecipeDao
import com.example.cookbook.data.models.Category
import com.example.cookbook.data.models.Recipe
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao, private val recipeDao: RecipeDao) {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories()

    suspend fun insertCategory(category: Category) {
        categoryDao.insert(category)
    }

    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> {
        return recipeDao.readRecipesByCategory(categoryId)
    }

    suspend fun insertRecipe(recipe: Recipe) {
        recipeDao.addRecipe(recipe)
    }
}
