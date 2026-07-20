package com.yuudev.wisatakebumen.model

data class WisataWeather(
    val idWisata: String,
    val weather: String,
    val temperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCode: Int,
    val rainProbability: Int
)
