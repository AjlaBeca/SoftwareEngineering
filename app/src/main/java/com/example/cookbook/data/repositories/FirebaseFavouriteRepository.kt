// FirebaseFavouriteRepository.kt
package com.example.cookbook.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.RecipeLikeCount
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


class FirebaseFavouriteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val favourites = firestore.collection("favourites")
    private val recipes   = firestore.collection("recipes")

    // Add a favourite
    suspend fun addFavourite(fav: Favourite): Boolean = try {
        val id = "${fav.recipeId}_${fav.userId}"
        favourites.document(id).set(fav).await()
        true
    } catch (e: Exception) {
        Log.e("FavRepo", "addFavourite failed", e)
        false
    }

    // Delete a favourite
    suspend fun deleteFavourite(fav: Favourite): Boolean = try {
        val id = "${fav.recipeId}_${fav.userId}"
        favourites.document(id).delete().await()
        true
    } catch (e: Exception) {
        Log.e("FavRepo", "deleteFavourite failed", e)
        false
    }

    // Observe single favourite state
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> {
        val live = MutableLiveData<Boolean>()
        val id = "${recipeId}_$userId"
        favourites.document(id)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FavRepo", "isFavourite error", err)
                    return@addSnapshotListener
                }
                live.value = snap?.exists() == true
            }
        return live
    }

    // Get all recipes this user has favourited
    fun getUserFavourites(userId: Long): LiveData<List<Recipe>> {
        val liveRecipes = MutableLiveData<List<Recipe>>()
        favourites
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FavRepo", "getUserFavourites error", err)
                    liveRecipes.value = emptyList()
                    return@addSnapshotListener
                }
                val favList = snap?.documents
                    ?.mapNotNull { it.toObject(Favourite::class.java) }
                    ?: emptyList()
                if (favList.isEmpty()) {
                    liveRecipes.value = emptyList()
                } else {
                    // fetch matching recipes
                    val ids = favList.map { it.recipeId }
                    recipes.whereIn("recipeId", ids)
                        .get()
                        .addOnSuccessListener { qs ->
                            liveRecipes.value = qs.documents
                                .mapNotNull { it.toObject(Recipe::class.java) }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FavRepo", "getUserFavourites: fetch recipes failed", e)
                            liveRecipes.value = emptyList()
                        }
                }
            }
        return liveRecipes
    }

    // Delete all favourites for a given recipe
    suspend fun deleteFavouritesByRecipeId(recipeId: Int): Boolean = try {
        val qs = favourites.whereEqualTo("recipeId", recipeId).get().await()
        qs.documents.forEach { it.reference.delete().await() }
        true
    } catch (e: Exception) {
        Log.e("FavRepo", "deleteFavouritesByRecipeId failed", e)
        false
    }

    // Get like counts for all recipes
    fun getRecipeLikeCounts(): LiveData<List<RecipeLikeCount>> {
        val liveCounts = MutableLiveData<List<RecipeLikeCount>>()
        favourites
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    Log.e("FavRepo", "getRecipeLikeCounts error", err)
                    return@addSnapshotListener
                }
                val allFavs = snap?.documents
                    ?.mapNotNull { it.toObject(Favourite::class.java) }
                    ?: emptyList()
                val countMap = allFavs.groupingBy { it.recipeId }
                    .eachCount()
                    .map { RecipeLikeCount(recipeId = it.key, likeCount = it.value) }
                liveCounts.value = countMap
            }
        return liveCounts
    }
}
