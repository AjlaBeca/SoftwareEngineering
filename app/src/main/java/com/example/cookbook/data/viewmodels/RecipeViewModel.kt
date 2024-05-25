package com.example.cookbook.data.viewmodels

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.repositories.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(private val recipeRepository: RecipeRepository) : ViewModel() {
    val readAllData: LiveData<List<Recipe>> = recipeRepository.readAllData

    var recipeName by mutableStateOf("")
        private set
    var recipeInstructions by mutableStateOf("")
        private set
    var recipeIngredients by mutableStateOf("")
        private set
    var recipeTime by mutableStateOf("")
        private set
    var recipeComplexity by mutableStateOf("Beginner")
        private set
    var recipeServings by mutableStateOf(1)
        private set
    var recipeAuthorId by mutableStateOf(1)
        private set
    var recipeImageUri by mutableStateOf<Uri?>(null)
        private set
    var recipeCategoryId by mutableStateOf(1)
        private set

    fun onRecipeNameChange(newName: String) {
        recipeName = newName
    }

    fun onRecipeInstructionsChange(newInstructions: String) {
        recipeInstructions = newInstructions
    }

    fun onRecipeIngredientsChange(newIngredients: String) {
        recipeIngredients = newIngredients
    }

    fun onRecipeTimeChange(newTime: String) {
        recipeTime = newTime
    }

    fun onRecipeComplexityChange(newComplexity: String) {
        recipeComplexity = newComplexity
    }

    fun onRecipeServingsChange(newServings: Int) {
        recipeServings = newServings
    }

    fun onImageUriChange(newUri: Uri?) {
        recipeImageUri = newUri
    }

    fun onRecipeCategoryIdChange(newCategoryId: Int) {
        recipeCategoryId = newCategoryId
    }

    fun isValidRecipe(): Boolean {
        Log.d("RecipeViewModel", "Checking if recipe is valid...")
        val isValid = recipeName.isNotBlank() && recipeInstructions.isNotBlank() && recipeIngredients.isNotBlank()
        Log.d("RecipeViewModel", "Recipe is valid: $isValid")
        return isValid
    }

    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> {
        return recipeRepository.getRecipesByCategory(categoryId)
    }
    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>> {
        return recipeRepository.getRecipesByUser(userId)
    }
    fun addRecipe(authorId: Long) {

        val newRecipe = Recipe(
            name = recipeName,
            instructions = recipeInstructions,
            ingredients = recipeIngredients,
            time = recipeTime,
            complexity = recipeComplexity,
            servings = recipeServings,
            authorId = authorId,
            categoryId = recipeCategoryId,
            imagePath = recipeImageUri?.toString(),
        )
        viewModelScope.launch {
            recipeRepository.addRecipe(newRecipe)
            resetFields()
        }
    }

    private fun resetFields() {
        recipeName = ""
        recipeInstructions = ""
        recipeIngredients = ""
        recipeTime = ""
        recipeComplexity = ""
        recipeServings = 1
        recipeAuthorId = 1
        recipeImageUri = null
        recipeCategoryId = 1
    }
    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeRepository.readAllData
    }
    class RecipeViewModelFactory(private val repository: RecipeRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}