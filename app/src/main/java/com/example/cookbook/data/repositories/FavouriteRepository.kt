package com.example.cookbook.data.repositories

import androidx.lifecycle.LiveData
import com.example.cookbook.data.dao.FavouriteDao
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe

class FavouriteRepository(private val favouriteDao: FavouriteDao) {

    suspend fun addFavourite(favourite: Favourite) {
        favouriteDao.insertFavourite(favourite)
    }

    suspend fun deleteFavourite(favourite: Favourite) {
        favouriteDao.deleteFavourite(favourite.recipeId, favourite.userId)
    }

    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean> {
        return favouriteDao.isFavourite(recipeId, userId)
    }

    fun getUserFavourites(userId: Long): LiveData<List<Recipe>> {
        return favouriteDao.getUserFavourites(userId)
    }

    suspend fun deleteFavouritesByRecipeId(recipeId: Int) {
        favouriteDao.deleteFavouritesByRecipeId(recipeId)
    }
}
