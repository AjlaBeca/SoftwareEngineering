package com.example.cookbook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.cookbook.data.dao.CategoryDao
import com.example.cookbook.data.dao.CategoryRecipeDao
import com.example.cookbook.data.dao.FavouriteDao
import com.example.cookbook.data.dao.RecipeDao
import com.example.cookbook.data.dao.UserDao
import com.example.cookbook.data.dao.UserRecipeDao
import com.example.cookbook.data.models.Category
import com.example.cookbook.data.models.CategoryRecipe
import com.example.cookbook.data.models.Favourite
import com.example.cookbook.data.models.Recipe
import com.example.cookbook.data.models.User
import com.example.cookbook.data.models.UserRecipe
@Database(
    entities = [Category::class, Recipe::class, User::class, Favourite::class, CategoryRecipe::class, UserRecipe::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun recipeDao(): RecipeDao
    abstract fun userDao(): UserDao
    abstract fun favouriteDao(): FavouriteDao
    abstract fun categoryRecipeDao(): CategoryRecipeDao
    abstract fun userRecipeDao(): UserRecipeDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
