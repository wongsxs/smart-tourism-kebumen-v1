package com.yuudev.wisatakebumen.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [WisataEntity::class, ReviewEntity::class, WeatherEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun wisataDao(): WisataDao
    abstract fun reviewDao(): ReviewDao
    abstract fun weatherDao(): WeatherDao
}
