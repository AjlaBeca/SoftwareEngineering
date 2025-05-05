// Update SharedPreferencesUtil.kt
package com.example.cookbook.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

object SharedPreferencesUtil {
    private const val PREFS_NAME = "cookbook_prefs"
    private const val KEY_IS_DARK_MODE = "is_dark_mode"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setDarkMode(context: Context, isDarkMode: Boolean) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_IS_DARK_MODE, isDarkMode)
        editor.apply()
    }

    fun isDarkMode(context: Context): Boolean {
        return getSharedPreferences(context).getBoolean(KEY_IS_DARK_MODE, false)
    }

    // Firebase Auth Integration
    fun isLoggedIn(context: Context): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    fun getUserId(context: Context): Long {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        return firebaseUser?.uid?.hashCode()?.toLong() ?: -1L
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, userId: Long? = null) {
        // This function is now a no-op as login state is managed by Firebase Auth
        // Kept for backward compatibility
        Log.d("SharedPreferencesUtil", "Login state is now managed by Firebase Auth")
    }
}