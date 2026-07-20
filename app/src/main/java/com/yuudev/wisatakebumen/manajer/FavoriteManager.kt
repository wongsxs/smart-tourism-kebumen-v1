package com.yuudev.wisatakebumen.manajer

import android.content.Context

object FavoriteManager {

    fun toggle(context: Context, key: String) {
        val pref = context.getSharedPreferences("fav", Context.MODE_PRIVATE)
        val isFav = pref.getBoolean(key, false)
        pref.edit().putBoolean(key, !isFav).apply()
    }

    fun isFavorite(context: Context, key: String): Boolean {
        return context.getSharedPreferences("fav", Context.MODE_PRIVATE)
            .getBoolean(key, false)
    }
}