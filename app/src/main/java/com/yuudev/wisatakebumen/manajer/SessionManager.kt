package com.yuudev.wisatakebumen.manajer

import android.content.Context

object SessionManager {

    fun saveLogin(
        context: Context,
        nama: String,
        username: String,
        role: String
    ) {

        context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .edit()
            .putString("nama", nama)
            .putString("username", username)
            .putString("role", role)
            .apply()
    }

    fun isLogin(
        context: Context
    ): Boolean {

        return context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .contains("username")
    }

    fun getRole(
        context: Context
    ): String {

        return context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .getString("role", "user")
            ?: "user"
    }

    fun logout(
        context: Context
    ) {

        context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .edit()
            .clear()
            .apply()
    }
    fun getNama(
        context: Context
    ): String {

        return context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .getString("nama", "")
            ?: ""
    }
    fun getUsername(
        context: Context
    ): String {

        return context
            .getSharedPreferences(
                "session",
                Context.MODE_PRIVATE
            )
            .getString("username", "")
            ?: ""
    }
}