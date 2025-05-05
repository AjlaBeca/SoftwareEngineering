package com.example.cookbook.data.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.cookbook.data.firebase.FirebaseAuthManager
import com.example.cookbook.data.models.User
import com.example.cookbook.utils.SharedPreferencesUtil
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val authManager = FirebaseAuthManager()
    private val auth = FirebaseAuth.getInstance()

    val currentUserId: MutableLiveData<Long?> = MutableLiveData(
        SharedPreferencesUtil.getUserId(application).takeIf { it != -1L }
    )

    val isLoggedIn = MutableLiveData(SharedPreferencesUtil.isLoggedIn(application))

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    init {
        // Set up auth state listener
        auth.addAuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            isLoggedIn.value = firebaseUser != null
            currentUserId.value = firebaseUser?.uid?.hashCode()?.toLong()

            viewModelScope.launch {
                _currentUser.value = if (firebaseUser != null) {
                    getUserById(firebaseUser.uid.hashCode().toLong())
                } else {
                    null
                }
            }
        }
    }

    suspend fun addUser(user: User): Boolean {
        return try {
            val result = authManager.signUp(user.email, user.password, user.username)
            result != null
        } catch (e: Exception) {
            false
        }
    }

    suspend fun login(email: String, password: String): User? {
        return authManager.login(email, password)
    }

    fun logout() {
        authManager.signOut()
    }

    suspend fun getUserByEmail(email: String): User? {
        // Firebase Auth doesn't have a direct method to get user by email
        // You'd need to query Firestore if you want to keep this functionality
        return null
    }

    suspend fun getUserById(userId: Long): User? {
        // Convert Long userId to Firebase UID string
        // This is an approximation and might need adjustment
        val userIdString = userId.toString()
        return authManager.getUserById(userIdString)
    }

    fun updateUserProfile(updatedUser: User) {
        viewModelScope.launch {
            authManager.updateProfile(updatedUser)
            refreshCurrentUser()
        }
    }

    fun refreshCurrentUser() {
        val firebaseUser = auth.currentUser
        if (firebaseUser != null) {
            viewModelScope.launch {
                _currentUser.value = getUserById(firebaseUser.uid.hashCode().toLong())
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