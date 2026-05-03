package com.yuudev.wisatakebumen

data class Wisata(
    val id: String = "",
    val nama: String = "",
    val deskripsi: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val image: String = "",
    val kategori: String = "" // 🔥 TAMBAHAN
)