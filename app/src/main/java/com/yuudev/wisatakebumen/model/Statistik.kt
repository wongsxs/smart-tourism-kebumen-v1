package com.yuudev.wisatakebumen.model

data class Statistik(
    val totalTiket: Int = 0,
    val totalPendapatan: Int = 0,
    val topWisata: List<List<Any>> = emptyList()
)