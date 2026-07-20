package com.yuudev.wisatakebumen.model.weather

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("current")
    val current: CurrentWeather,
    @SerializedName("hourly")
    val hourly: HourlyWeather?,
    @SerializedName("daily")
    val daily: DailyWeather?
)
