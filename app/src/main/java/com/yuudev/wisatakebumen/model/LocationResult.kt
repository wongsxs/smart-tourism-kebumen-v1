package com.yuudev.wisatakebumen.model

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)