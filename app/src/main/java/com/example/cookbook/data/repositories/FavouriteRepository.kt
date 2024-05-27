package com.example.cookbook.data.repositories

import androidx.lifecycle.LiveData
import com.example.cookbook.data.dao.FavouriteDao
import com.example.cookbook.data.models.Favourite

class FavouriteRepository(private val favouriteDao: FavouriteDao) {
    suspend fun addFavourite(favourite: Favourite) = favouriteDao.addFavourite(favourite)
    suspend fun deleteFavourite(favourite: Favourite) = favouriteDao.deleteFavourite(favourite)
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> {
        return favouriteDao.isFavourite(recipeId, userId)
    }
}