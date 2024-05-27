
package com.example.cookbook.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.cookbook.data.models.Favourite

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addFavourite(favourite: Favourite)

    @Query("SELECT * FROM Favourite ORDER BY favouriteId ASC")
    fun readAllData(): LiveData<List<Favourite>>

    @Delete
    suspend fun deleteFavourite(favourite: Favourite)

    @Query("SELECT COUNT(*) > 0 FROM Favourite WHERE recipeId = :recipeId AND userId = :userId")
    fun isFavourite(recipeId: Int, userId: Long): LiveData<Boolean>
}