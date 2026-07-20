package com.yuudev.wisatakebumen.network.weather

import com.yuudev.wisatakebumen.model.weather.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("v1/forecast")
    suspend fun getWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,precipitation_probability",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto"
    ): WeatherResponse
}
