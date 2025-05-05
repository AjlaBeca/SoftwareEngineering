package com.example.cookbook.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.cookbook.data.models.User

class FirebaseAuthManager {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")

    // Convert FirebaseUser to User model
    private fun FirebaseUser.toUser(): User {
        return User(
            userId = this.uid.hashCode().toLong(),
            email = this.email ?: "",
            username = this.displayName ?: this.email?.substringBefore("@") ?: "",
            password = "", // No longer storing password
            image = this.photoUrl?.toString()
        )
    }

    // Sign up new user
    suspend fun signUp(email: String, password: String, username: String): User? {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null

            // Store additional user info in Firestore
            val user = User(
                userId = firebaseUser.uid.hashCode().toLong(),
                email = email,
                password = "", // Don't store password
                username = username,
                image = null
            )

            usersCollection.document(firebaseUser.uid).set(user).await()
            user
        } catch (e: Exception) {
            null
        }
    }

    // Log in existing user
    suspend fun login(email: String, password: String): User? {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: return null

            // Get user profile from Firestore
            val documentSnapshot = usersCollection.document(firebaseUser.uid).get().await()
            documentSnapshot.toObject(User::class.java) ?: firebaseUser.toUser()
        } catch (e: Exception) {
            null
        }
    }

    // Get current user
    fun getCurrentUser(): User? {
        return auth.currentUser?.toUser()
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }

    // Update user profile
    suspend fun updateProfile(user: User): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            usersCollection.document(currentUser.uid).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Get user by ID
    suspend fun getUserById(userId: String): User? {
        return try {
            val documentSnapshot = usersCollection.document(userId).get().await()
            documentSnapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }
}