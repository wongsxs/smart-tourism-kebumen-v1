package com.yuudev.wisatakebumen.model.weather

import com.google.gson.annotations.SerializedName

data class DailyWeather(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("sunrise")
    val sunrise: List<String>,
    @SerializedName("sunset")
    val sunset: List<String>
)
