// Create FirebaseRecipeRepository.kt
package com.example.cookbook.data.repositories

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.example.cookbook.data.models.Recipe
import java.util.UUID

class FirebaseRecipeRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val recipesCollection = firestore.collection("recipes")
    private val storageRef = storage.reference.child("recipe_images")

    // Get all recipes
    fun getAllRecipes(): LiveData<List<Recipe>> {
        val recipesLiveData = MutableLiveData<List<Recipe>>()

        recipesCollection
            .orderBy("recipeId", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRecipe", "Error fetching recipes", error)
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull {
                    it.toObject(Recipe::class.java)
                } ?: emptyList()

                recipesLiveData.value = recipes
            }

        return recipesLiveData
    }

    // Get recipes by category
    fun getRecipesByCategory(categoryId: Int): LiveData<List<Recipe>> {
        val recipesLiveData = MutableLiveData<List<Recipe>>()

        recipesCollection
            .whereEqualTo("categoryId", categoryId)
            .orderBy("recipeId", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRecipe", "Error fetching recipes by category", error)
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull {
                    it.toObject(Recipe::class.java)
                } ?: emptyList()

                recipesLiveData.value = recipes
            }

        return recipesLiveData
    }

    // Get recipes by user
    fun getRecipesByUser(userId: Long): LiveData<List<Recipe>> {
        val recipesLiveData = MutableLiveData<List<Recipe>>()

        recipesCollection
            .whereEqualTo("authorId", userId)
            .orderBy("recipeId", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseRecipe", "Error fetching recipes by user", error)
                    return@addSnapshotListener
                }

                val recipes = snapshot?.documents?.mapNotNull {
                    it.toObject(Recipe::class.java)
                } ?: emptyList()

                recipesLiveData.value = recipes
            }

        return recipesLiveData
    }

    // Add recipe with image upload
    suspend fun addRecipe(recipe: Recipe, imageUri: Uri?): Boolean {
        return try {
            // First, upload the image if provided
            val imagePath = if (imageUri != null) {
                val fileName = "recipe_${UUID.randomUUID()}"
                val ref = storageRef.child(fileName)
                ref.putFile(imageUri).await()
                ref.downloadUrl.await().toString()
            } else {
                recipe.imagePath
            }

            // Create recipe with updated image path
            val newRecipe = recipe.copy(imagePath = imagePath)

            // Generate a new ID if needed
            val recipeId = recipe.recipeId ?: (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
            val recipeWithId = newRecipe.copy(recipeId = recipeId)

            // Add to Firestore
            recipesCollection.document(recipeId.toString()).set(recipeWithId).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRecipe", "Error adding recipe", e)
            false
        }
    }

    // Delete recipe
    suspend fun deleteRecipe(recipeId: Int): Boolean {
        return try {
            recipesCollection.document(recipeId.toString()).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseRecipe", "Error deleting recipe", e)
            false
        }
    }
}