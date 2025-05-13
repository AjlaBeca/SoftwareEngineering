import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cookbook.R
import com.example.cookbook.data.AppDatabase
import com.example.cookbook.data.dao.RecipeLikeCount
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.repositories.FavouriteRepository
import com.example.cookbook.data.repositories.RecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: RecipeRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {
    val readAllData: LiveData<List<Recipe>> = recipeRepository.readAllData

    val recipeLikeCounts: LiveData<List<RecipeLikeCount>> = favouriteRepository.getRecipeLikeCounts()
    private val _isFavourite = MutableLiveData<Boolean>()

    fun fetchInitialFavouriteStatus(recipeId: Int, userId: Long) {
        viewModelScope.launch {
            val favouriteStatus = favouriteRepository.isFavourite(recipeId, userId)
            _isFavourite.postValue(favouriteStatus.value)
        }
    }

    fun addFavourite(favourite: Favourite) {
        viewModelScope.launch {
            favouriteRepository.addFavourite(favourite)
        }
    }

    fun deleteFavourite(favourite: Favourite) {
        viewModelScope.launch {
            favouriteRepository.deleteFavourite(favourite)
        }
    }

    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> {
        return favouriteRepository.isFavourite(recipeId, userId)
    }

    fun getUserFavourites(userId: Long): LiveData<List<Recipe>> {
        return favouriteRepository.getUserFavourites(userId)
    }

    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> {
        return recipeRepository.getRecipesByCategory(categoryId)
    }

    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>> {
        return recipeRepository.getRecipesByUser(userId)
    }

    fun getAllRecipes(): LiveData<List<Recipe>> {
        return recipeRepository.readAllData
    }

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
        val isValid =
            recipeName.isNotBlank() && recipeInstructions.isNotBlank() && recipeIngredients.isNotBlank()
        Log.d("RecipeViewModel", "Recipe is valid: $isValid")
        return isValid
    }

    private val placeholderImageUri: Uri? =
        Uri.parse("android.resource://com.example.cookbook/${R.drawable.placeholder}")

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
            imagePath = recipeImageUri?.toString() ?: placeholderImageUri?.toString(),
        )
        viewModelScope.launch {
            try {
                recipeRepository.addRecipe(newRecipe)
                resetFields()
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error adding recipe: ${e.message}")
            }
        }
    }

    fun deleteRecipe(recipeId: Int) {
        viewModelScope.launch {
            recipeRepository.deleteRecipe(recipeId)
            favouriteRepository.deleteFavouritesByRecipeId(recipeId)
        }
    }

    private fun resetFields() {
        recipeName = ""
        recipeInstructions = ""
        recipeIngredients = ""
        recipeTime = ""
        recipeComplexity = "Beginner"
        recipeServings = 1
        recipeAuthorId = 1
        recipeImageUri = null
        recipeCategoryId = 1
    }

    class RecipeViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(RecipeViewModel::class.java)) {
                val db = AppDatabase.getDatabase(application)
                val recipeDao = db.recipeDao()
                val favouriteDao = db.favouriteDao()
                val recipeRepository = RecipeRepository(recipeDao)
                val favouriteRepository = FavouriteRepository(favouriteDao)
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(recipeRepository, favouriteRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
