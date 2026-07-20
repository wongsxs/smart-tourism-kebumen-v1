package com.yuudev.wisatakebumen.model

data class Review(
    val id: Int,
    val wisataId: String,
    val username: String,
    val nama: String,
    val rating: Int,
    val komentar: String,
    val tanggal: String
)