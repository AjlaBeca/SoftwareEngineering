package com.example.cookbook.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
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
}
