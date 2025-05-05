// Create FirebaseCategoryRepository.kt
package com.example.cookbook.data.repositories

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.cookbook.data.models.Category

class FirebaseCategoryRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val categoriesCollection = firestore.collection("categories")

    // Get all categories
    fun getAllCategories(): LiveData<List<Category>> {
        val categoriesLiveData = MutableLiveData<List<Category>>()

        categoriesCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("FirebaseCategory", "Error fetching categories", error)
                return@addSnapshotListener
            }

            val categories = snapshot?.documents?.mapNotNull {
                it.toObject(Category::class.java)
            } ?: emptyList()

            categoriesLiveData.value = categories
        }

        return categoriesLiveData
    }

    // Insert a category
    suspend fun insert(category: Category): Boolean {
        return try {
            categoriesCollection.document(category.categoryId.toString()).set(category).await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseCategory", "Error inserting category", e)
            false
        }
    }

    // Insert multiple categories
    suspend fun insertAll(categories: List<Category>): Boolean {
        return try {
            val batch = firestore.batch()

            categories.forEach { category ->
                val docRef = categoriesCollection.document(category.categoryId.toString())
                batch.set(docRef, category)
            }

            batch.commit().await()
            true
        } catch (e: Exception) {
            Log.e("FirebaseCategory", "Error inserting multiple categories", e)
            false
        }
    }
}