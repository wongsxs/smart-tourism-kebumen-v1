package com.yuudev.wisatakebumen.model

data class TripRequest(
    val action: String = "tripPlanner",
    val budget: Int,
    val duration: Int,
    val people: Int,
    val vehicle: String,
    val startLatitude: Double,
    val startLongitude: Double,
    val interests: List<String>,
    val withChildren: Boolean,
    val startTime: String,
    val weather: List<WisataWeather> = emptyList()
)
