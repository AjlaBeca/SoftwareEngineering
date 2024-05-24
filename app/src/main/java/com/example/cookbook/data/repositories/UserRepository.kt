package com.example.cookbook.data.repositories

import android.app.Application
import androidx.lifecycle.LiveData
import com.example.cookbook.data.dao.UserDao
import com.example.cookbook.data.models.User
import com.example.cookbook.data.AppDatabase // Ensure this import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(application: Application) {
    private val userDao: UserDao
    val readAllData: LiveData<List<User>>

    init {
        val database = AppDatabase.getDatabase(application)
        userDao = database.userDao()
        readAllData = userDao.readAllData()
    }

    suspend fun addUser(user: User) {
        withContext(Dispatchers.IO) {
            userDao.addUser(user)
        }
    }

    suspend fun getUserByEmail(email: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.getUserByEmail(email)
        }
    }
}
