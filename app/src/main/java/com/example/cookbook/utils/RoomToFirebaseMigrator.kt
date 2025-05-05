// Create RoomToFirebaseMigrator.kt
package com.example.cookbook.utils

import android.content.Context
import android.util.Log
import com.example.cookbook.data.AppDatabase
import com.example.cookbook.data.repositories.FirebaseCategoryRepository
import com.example.cookbook.data.repositories.FirebaseFavouriteRepository
import com.example.cookbook.data.repositories.FirebaseRecipeRepository
import com.example.cookbook.data.firebase.FirebaseAuthManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RoomToFirebaseMigrator(private val context: Context) {
    private val TAG = "DataMigrator"
    private val database = AppDatabase.getDatabase(context)

    private val categoryRepo = FirebaseCategoryRepository()
    private val recipeRepo = FirebaseRecipeRepository()
    private val favouriteRepo = FirebaseFavouriteRepository()
    private val authManager = FirebaseAuthManager()

    suspend fun migrateAllData() {
        withContext(Dispatchers.IO) {
            try {
                // Migrate categories first
                migrateCategories()

                // Then users
                migrateUsers()

                // Then recipes
                migrateRecipes()

                // Finally favourites which depend on recipes
                migrateFavourites()

                Log.d(TAG, "Migration completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Migration failed", e)
            }
        }
    }

    private suspend fun migrateCategories() {
        val categories = database.categoryDao().getAllCategories().value ?: return
        categoryRepo.insertAll(categories)
        Log.d(TAG, "Migrated ${categories.size} categories")
    }

    private suspend fun migrateUsers() {
        val users = database.userDao().readAllData().value ?: return

        for (user in users) {
            // Create auth account
            val result = authManager.signUp(user.email, user.password, user.username)
            if (result != null) {
                Log.d(TAG, "Migrated user: ${user.email}")
            } else {
                Log.e(TAG, "Failed to migrate user: ${user.email}")
            }
        }
    }

    private suspend fun migrateRecipes() {
        val recipes = database.recipeDao().readAllData().value ?: return

        for (recipe in recipes) {
            val imageUri = recipe.imagePath?.let { android.net.Uri.parse(it) }
            val success = recipeRepo.addRecipe(recipe, imageUri)
            if (success) {
                Log.d(TAG, "Migrated recipe: ${recipe.name}")
            } else {
                Log.e(TAG, "Failed to migrate recipe: ${recipe.name}")
            }
        }
    }

    private suspend fun migrateFavourites() {
        // This is a simplified approach - in a real app you would need to extract
        // favourites from Room and add them to Firebase
        Log.d(TAG, "Skipping favourites migration - would need custom implementation")
    }
}