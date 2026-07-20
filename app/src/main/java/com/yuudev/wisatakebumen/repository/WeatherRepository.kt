package com.yuudev.wisatakebumen.repository

import android.content.Context
import com.yuudev.wisatakebumen.data.DatabaseProvider
import com.yuudev.wisatakebumen.data.WeatherEntity
import com.yuudev.wisatakebumen.model.weather.WeatherResponse
import com.yuudev.wisatakebumen.model.weather.CurrentWeather
import com.yuudev.wisatakebumen.model.weather.HourlyWeather
import com.yuudev.wisatakebumen.model.weather.DailyWeather
import com.yuudev.wisatakebumen.network.weather.WeatherApiClient
import com.yuudev.wisatakebumen.util.CachePolicy
import com.yuudev.wisatakebumen.util.NetworkMonitor

class WeatherRepository(private val context: Context) {
    private val dao = DatabaseProvider.getDatabase(context).weatherDao()
    private val networkMonitor = NetworkMonitor(context)

    suspend fun getWeather(latitude: Double, longitude: Double, force: Boolean = false): WeatherResponse {
        val latLng = "${latitude},${longitude}"
        val prefs = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)
        val key = "last_weather_sync_$latLng"
        val lastSync = prefs.getLong(key, 0L)
        val now = System.currentTimeMillis()

        val cachedEntity = dao.getWeather(latLng)
        
        if (cachedEntity != null && !force && (now - lastSync <= CachePolicy.WEATHER_CACHE_DURATION)) {
            return cachedEntity.toWeatherResponse()
        }

        if (networkMonitor.isNetworkAvailable()) {
            val response = WeatherApiClient.instance.getWeather(latitude = latitude, longitude = longitude)
            val entity = WeatherEntity(
                latLng = latLng,
                latitude = latitude,
                longitude = longitude,
                currentTemperature = response.current.temperature,
                currentWeatherCode = response.current.weatherCode,
                currentWindSpeed = response.current.windSpeed,
                currentRelativeHumidity = response.current.relativeHumidity,
                hourlyTime = response.hourly?.time?.joinToString(",") ?: "",
                hourlyTemperature2m = response.hourly?.temperature2m?.joinToString(",") ?: "",
                hourlyWeatherCode = response.hourly?.weatherCode?.joinToString(",") ?: "",
                hourlyPrecipitationProbability = response.hourly?.precipitationProbability?.joinToString(",") ?: "",
                dailyTime = response.daily?.time?.joinToString(",") ?: "",
                dailySunrise = response.daily?.sunrise?.joinToString(",") ?: "",
                dailySunset = response.daily?.sunset?.joinToString(",") ?: ""
            )
            dao.insertWeather(entity)
            prefs.edit().putLong(key, now).apply()
            return response
        } else {
            if (cachedEntity != null) {
                return cachedEntity.toWeatherResponse()
            } else {
                throw Exception("Tidak ada koneksi internet dan tidak ada data tersimpan")
            }
        }
    }
}
