// Create FirebaseFavouriteRepository.kt
package com.example.cookbook.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.dao.RecipeLikeCount

class FirebaseFavouriteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val favouritesCollection = firestore.collection("favourites")
    private val recipesCollection = firestore.collection("recipes")

    // Add favourite
    suspend fun addFavourite(favourite: Favourite): Boolean {
        return try {
            val favouriteId = "${favourite.recipeId}_${favourite.userId}"
            favouritesCollection.document(favouriteId).set(favourite).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseFavourite", "Error adding favourite", e)
            false
        }
    }

    // Delete favourite
    suspend fun deleteFavourite(favourite: Favourite): Boolean {
        return try {
            val favouriteId = "${favourite.recipeId}_${favourite.userId}"
            favouritesCollection.document(favouriteId).delete().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseFavourite", "Error deleting favourite", e)
            false
        }
    }

    // Check if recipe is favourited by user
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> {
        val isFavouriteLiveData = MutableLiveData<Boolean>()

        val favouriteId = "${recipeId}_${userId}"
        favouritesCollection.document(favouriteId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseFavourite", "Error checking favourite status", error)
                return@addSnapshotListener
            }

            isFavouriteLiveData.value = snapshot?.exists() ?: false
        }

        return isFavouriteLiveData
    }

    // Get user's favourite recipes
    fun getUserFavourites(userId: Long): LiveData<List<Recipe>> {
        val favouritesLiveData = MutableLiveData<List<Recipe>>()

        favouritesCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirebaseFavourite", "Error fetching user favourites", error)
                    return@addSnapshotListener
                }

                val favourites = snapshot?.documents?.mapNotNull {
                    it.toObject(Favourite::class.java)
                } ?: emptyList()

                if (favourites.isEmpty()) {
                    favouritesLiveData.value = emptyList()
                    return@addSnapshotListener
                }

                // Get all recipe IDs from favourites
                val recipeIds = favourites.map { it.recipeId }

                // Fetch recipes with these IDs
                recipesCollection
                    .whereIn("recipeId", recipeIds)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val recipes = querySnapshot.documents.mapNotNull {
                            it.toObject(Recipe::class.java)
                        }
                        favouritesLiveData.value = recipes
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseFavourite", "Error fetching favourite recipes", e)
                        favouritesLiveData.value = emptyList()
                    }
            }

        return favouritesLiveData
    }

    // Delete favourites for a recipe
    suspend fun deleteFavouritesByRecipeId(recipeId: Int): Boolean {
        return try {
            val querySnapshot = favouritesCollection
                .whereEqualTo("recipeId", recipeId)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.reference.delete().await()
            }

            true
        } catch (e: Exception) {
            Log.e("FirebaseFavourite", "Error deleting favourites for recipe", e)
            false
        }
    }

    // Get recipe like counts
    fun getRecipeLikeCounts(): LiveData<List<RecipeLikeCount>> {
        val likesLiveData = MutableLiveData<List<RecipeLikeCount>>()

        favouritesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseFavourite", "Error fetching like counts", error)
                return@addSnapshotListener
            }

            val likes = snapshot?.documents?.mapNotNull {
                it.toObject(Favourite::class.java)
            } ?: emptyList()

            // Calculate counts by recipe ID
            val likeCountMap = likes.groupBy { it.recipeId }
                .mapValues { it.value.size }
                .map { RecipeLikeCount(it.key, it.value) }

            likesLiveData.value = likeCountMap
        }

        return likesLiveData
    }
}