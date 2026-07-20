package com.yuudev.wisatakebumen.model

data class Wisata(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val image: String = "",
    val kategori: String = "", // 🔥 TAMBAHAN
    val harga: String = "",
    val rating: Double = 0.0,
    val totalReview: Int = 0
)