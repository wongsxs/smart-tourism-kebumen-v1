package com.yuudev.wisatakebumen.model

data class ReviewResponse(
    val rating: Double,
    val totalReview: Int,
    val reviews: List<Review>
)