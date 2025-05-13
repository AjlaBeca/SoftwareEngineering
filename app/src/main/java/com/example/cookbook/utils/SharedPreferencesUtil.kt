package com.example.cookbook.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

object SharedPreferencesUtil {
    private const val PREFS_NAME = "cookbook_prefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_ID = "user_id"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setLoggedIn(context: Context, isLoggedIn: Boolean, userId: Long? = null) {
        val editor = getSharedPreferences(context).edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn)
        if (isLoggedIn) {
            editor.putLong(KEY_USER_ID, userId ?: -1)
        } else {
            editor.remove(KEY_USER_ID)
        }
        editor.apply()
        Log.d("SharedPreferencesUtil", "setLoggedIn: $isLoggedIn, userId: $userId")
    }

    fun isLoggedIn(context: Context): Boolean {
        val isLoggedIn = getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
        Log.d("SharedPreferencesUtil", "isLoggedIn: $isLoggedIn")
        return isLoggedIn
    }

    fun getUserId(context: Context): Long {
        return getSharedPreferences(context).getLong(KEY_USER_ID, -1)
    }
}
