package com.yuudev.wisatakebumen.model.weather

import com.google.gson.annotations.SerializedName

data class HourlyWeather(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("precipitation_probability")
    val precipitationProbability: List<Int>
)
