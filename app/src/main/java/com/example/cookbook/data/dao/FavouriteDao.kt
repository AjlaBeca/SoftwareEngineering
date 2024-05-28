package com.example.cookbook.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe

@Dao
interface FavouriteDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFavourite(favourite: Favourite)

    @Query("DELETE FROM Favourite WHERE recipeId = :recipeId AND userId = :userId")
    suspend fun deleteFavourite(recipeId: Int, userId: Long)

    @Query("SELECT COUNT(*) > 0 FROM Favourite WHERE recipeId = :recipeId AND userId = :userId")
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean>

    @Query("SELECT recipes.* FROM recipes INNER JOIN Favourite ON recipes.recipeId = Favourite.recipeId WHERE Favourite.userId = :userId")
    fun getUserFavourites(userId: Long): LiveData<List<Recipe>>

    @Query("DELETE FROM Favourite WHERE recipeId = :recipeId")
    suspend fun deleteFavouritesByRecipeId(recipeId: Int)
}
