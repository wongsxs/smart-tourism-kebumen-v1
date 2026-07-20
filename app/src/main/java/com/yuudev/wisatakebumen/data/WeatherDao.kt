package com.yuudev.wisatakebumen.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE latLng = :latLng")
    fun getWeatherStream(latLng: String): Flow<WeatherEntity?>

    @Query("SELECT * FROM weather WHERE latLng = :latLng")
    suspend fun getWeather(latLng: String): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)
}
