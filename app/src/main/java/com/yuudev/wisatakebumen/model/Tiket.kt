package com.yuudev.wisatakebumen.model

data class Tiket(
    val bookingId: String = "",
    val namaWisata: String = "",
    val namaPemesan: String = "",
    val tanggalKunjungan: String = "",
    val jumlahTiket: String = "",
    val totalBayar: String = "",
    val status: String = "",
    val createdAt: String = ""
)