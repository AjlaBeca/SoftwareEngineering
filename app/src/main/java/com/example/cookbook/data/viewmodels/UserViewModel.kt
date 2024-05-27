package com.example.cookbook.data.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cookbook.data.models.User
import com.example.cookbook.data.repositories.UserRepository
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: UserRepository = UserRepository(application)
    val readAllData: LiveData<List<User>> = repository.readAllData
    val currentUserId: MutableLiveData<Long?> = MutableLiveData(
        SharedPreferencesUtil.getUserId(application).takeIf { it != -1L }
    )
    val isLoggedIn = MutableLiveData(SharedPreferencesUtil.isLoggedIn(application))

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        currentUserId.observeForever { userId ->
            viewModelScope.launch {
                _currentUser.value = userId?.let { getUserById(it) }
            }
        }
    }

    suspend fun addUser(user: User): Boolean {
        return try {
            repository.addUser(user)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, password: String): User? {
        val user = repository.getUserByEmail(email)?.takeIf { it.password == password }
        if (user != null) {
            currentUserId.postValue(user.userId)
            SharedPreferencesUtil.setLoggedIn(getApplication(), true, user.userId)
            isLoggedIn.postValue(true)
        }
        return user
    }

    fun logout() {
        currentUserId.postValue(null)
        SharedPreferencesUtil.setLoggedIn(getApplication(), false)
        isLoggedIn.postValue(false)
    }

    suspend fun getUserByEmail(email: String): User? {
        return repository.getUserByEmail(email)
    }

    suspend fun getUserById(userId: Long): User? {
        return repository.getUserById(userId)
    }

    fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            repository.updateUser(updatedUser)
            refreshCurrentUser()
        }
    }

    fun refreshCurrentUser() {
        currentUserId.value?.let {
            viewModelScope.launch {
                _currentUser.value = getUserById(it)
            }
        }
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
