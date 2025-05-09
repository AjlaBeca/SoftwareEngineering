// RecipeViewModel.kt
package com.example.cookbook.data.viewmodels

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.*
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.RecipeLikeCount
import com.example.cookbook.data.repositories.FirebaseFavouriteRepository
import com.example.cookbook.data.repositories.FirebaseRecipeRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipesRepo: FirebaseRecipeRepository,
    private val favsRepo: FirebaseFavouriteRepository
) : ViewModel() {

    // All recipes
    val allRecipes: LiveData<List<Recipe>> = recipesRepo.getAllRecipes()

    private val db = FirebaseFirestore.getInstance()
    private val _isFavouriteMap = mutableMapOf<Pair<Int, Long>, MutableLiveData<Boolean>>()

    // Like counts
    val recipeLikeCounts: LiveData<List<RecipeLikeCount>> = favsRepo.getRecipeLikeCounts()

    // Expose filtered helpers
    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> =
        recipesRepo.getRecipesByCategory(categoryId)

    fun getAllRecipes(): LiveData<List<Recipe>> = allRecipes

    // Mutable state for form fields
    var recipeName         by mutableStateOf("")
        private set
    var recipeInstructions by mutableStateOf("")
        private set
    var recipeIngredients  by mutableStateOf("")
        private set
    var recipeTime         by mutableStateOf("")
        private set
    var recipeComplexity   by mutableStateOf("Beginner")
        private set
    var recipeServings     by mutableStateOf(1)
        private set
    var recipeImageUri     by mutableStateOf<Uri?>(null)
        private set
    var recipeCategoryId   by mutableStateOf(1)
        private set

    // Field updates
    fun onRecipeNameChange(v: String)         { recipeName = v }
    fun onRecipeInstructionsChange(v: String) { recipeInstructions = v }
    fun onRecipeIngredientsChange(v: String)  { recipeIngredients = v }
    fun onRecipeTimeChange(v: String)         { recipeTime = v }
    fun onRecipeComplexityChange(v: String)   { recipeComplexity = v }
    fun onRecipeServingsChange(v: Int)        { recipeServings = v }
    fun onImageUriChange(v: Uri?)             { recipeImageUri = v }
    fun onCategoryIdChange(v: Int)            { recipeCategoryId = v }

    // Validate
    fun isValidRecipe(): Boolean {
        val ok = recipeName.isNotBlank()
                && recipeInstructions.isNotBlank()
                && recipeIngredients.isNotBlank()
        Log.d("RecipeVM", "Valid: $ok")
        return ok
    }

    // Add a new recipe
    fun addRecipe(authorId: Long) {
        val r = Recipe(
            name        = recipeName,
            instructions= recipeInstructions,
            ingredients = recipeIngredients,
            time        = recipeTime,
            complexity  = recipeComplexity,
            servings    = recipeServings,
            authorId    = authorId,
            categoryId  = recipeCategoryId,
            imagePath   = null
        )
        viewModelScope.launch {
            recipesRepo.addRecipe(r, recipeImageUri)
            resetFields()
        }
    }

    // Delete a recipe (and its favourites)
    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            recipesRepo.deleteRecipe(recipeId)
            favsRepo.deleteFavouritesByRecipeId(recipeId)
        }
    }

    // Favourite operations
    fun addFavourite(fav: Favourite) = viewModelScope.launch {
        favsRepo.addFavourite(fav)
    }
    fun deleteFavourite(fav: Favourite) = viewModelScope.launch {
        favsRepo.deleteFavourite(fav)
    }
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> =
        favsRepo.isFavourite(recipeId, userId)

    fun getUserFavourites(userId: Long): LiveData<List<Recipe>> =
        favsRepo.getUserFavourites(userId)

    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>> =
        recipesRepo.getRecipesByUser(userId)

    fun fetchInitialFavouriteStatus(recipeId: Int, userId: Long): LiveData<Boolean> {
        val key = Pair(recipeId, userId)

        val existingLiveData = _isFavouriteMap[key]
        if (existingLiveData != null) return existingLiveData

        val liveData = MutableLiveData<Boolean>()
        _isFavouriteMap[key] = liveData

        db.collection("favourites")
            .whereEqualTo("recipeId", recipeId)
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val isFav = !querySnapshot.isEmpty
                liveData.postValue(isFav)
            }
            .addOnFailureListener {
                liveData.postValue(false)
            }

        return liveData
    }


    private fun resetFields() {
        recipeName = ""
        recipeInstructions = ""
        recipeIngredients = ""
        recipeTime = ""
        recipeComplexity = "Beginner"
        recipeServings = 1
        recipeImageUri = null
        recipeCategoryId = 1
    }

    class RecipeViewModelFactory(private val app: Application) :
        ViewModelProvider.AndroidViewModelFactory(app) {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                val repo = FirebaseRecipeRepository()
                val favs = FirebaseFavouriteRepository()
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(repo, favs) as T
            }
            return super.create(modelClass)
        }
    }
}
