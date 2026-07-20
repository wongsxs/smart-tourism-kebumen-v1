package com.yuudev.wisatakebumen.model

data class TripPlanResponse(
    val title: String,
    val summary: String,
    val estimatedCost: Int,
    val remainingBudget: Int,
    val estimatedDistance: String,
    val estimatedDuration: String,
    val weatherRecommendation: String,
    val tips: List<String>,
    val restaurants: List<String>,
    val destinations: List<DestinationPlan>
)

data class DestinationPlan(
    val idWisata: String,
    val nama: String,
    val deskripsi: String,
    val arrivalTime: String,
    val departureTime: String,
    val duration: Int,
    val ticketPrice: Int,
    val latitude: Double,
    val longitude: Double,
    val rating: Double
)
