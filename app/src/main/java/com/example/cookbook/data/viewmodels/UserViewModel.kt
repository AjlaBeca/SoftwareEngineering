// UserViewModel.kt
package com.example.cookbook.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cookbook.data.models.User
import com.example.cookbook.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepository(application)
    val readAllData: LiveData<List<User>> = repository.readAllData

    suspend fun addUser(user: User): Boolean {
        return try {
            repository.addUser(user)
            true // Indicates successful addition
        } catch (e: Exception) {
            false // Indicates failure
        }
    }


    suspend fun login(email: String, password: String): User? {
        return repository.getUserByEmail(email)?.takeIf { it.password == password }
    }


    class UserViewModelFactory(private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return UserViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
