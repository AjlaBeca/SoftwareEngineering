package com.example.cookbook.data.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.cookbook.data.AppDatabase
import com.example.cookbook.data.dao.FavouriteDao
import com.example.cookbook.data.models.Favourite

class FavouriteRepository(application: Application)  {
    /*
    private val favouriteDao: FavouriteDao
    val readAllData: LiveData<List<Favourite>>

     */
    /*
    init {
        val database = AppDatabase.getDatabase(application)
        favouriteDao = database.favouriteDao()
        readAllData = favouriteDao.readAllData()
    }
    suspend fun addFavourite(favourite: Favourite) {
        favouriteDao.addFavourite(favourite)
    }
    /*
    suspend fun isRecipeInFavorites(recipeId: Int, userId: Long): Boolean {
        return favouriteDao.isRecipeInFavorites(recipeId, userId)
    }
*/*/
}