package com.yuudev.wisatakebumen.model.weather

import com.google.gson.annotations.SerializedName

data class CurrentWeather(
    @SerializedName("temperature_2m")
    val temperature: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity: Int
)
