import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.cookbook.data.dao.RecipeLikeCount
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.repositories.FirebaseFavouriteRepository
import com.example.cookbook.data.repositories.FirebaseRecipeRepository
import kotlinx.coroutines.launch

class RecipeViewModel(
    private val recipeRepository: FirebaseRecipeRepository,
    private val favouriteRepository: FirebaseFavouriteRepository
) : ViewModel() {
    val readAllData: LiveData<List<Recipe>> = recipeRepository.getAllRecipes()

    val recipeLikeCounts: LiveData<List<RecipeLikeCount>> = favouriteRepository.getRecipeLikeCounts()
    private val _isFavourite = MutableLiveData<Boolean>()

    fun fetchInitialFavouriteStatus(recipeId: Int, userId: Long) {
        // The Firebase repository handles this with LiveData
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
        return recipeRepository.getAllRecipes()
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
            imagePath = null // Will be set by the repository
        )

        viewModelScope.launch {
            try {
                recipeRepository.addRecipe(newRecipe, recipeImageUri)
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
                val recipeRepository = FirebaseRecipeRepository()
                val favouriteRepository = FirebaseFavouriteRepository()
                @Suppress("UNCHECKED_CAST")
                return RecipeViewModel(recipeRepository, favouriteRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}