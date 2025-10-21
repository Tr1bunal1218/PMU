package com.example.androidgamekt.data

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    fun setCurrentUserId(userId: Long) {
        prefs.edit().putLong(KEY_CURRENT_USER_ID, userId).apply()
    }

    fun getCurrentUserId(): Long? {
        val id = prefs.getLong(KEY_CURRENT_USER_ID, -1L)
        return if (id == -1L) null else id
    }

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
}


