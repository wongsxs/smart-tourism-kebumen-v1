package com.yuudev.wisatakebumen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yuudev.wisatakebumen.model.Wisata

@Entity(tableName = "wisata")
data class WisataEntity(
    @PrimaryKey val id: String,
    val nama: String,
    val deskripsi: String,
    val lat: Double,
    val lng: Double,
    val image: String,
    val kategori: String,
    val harga: String,
    val rating: Double,
    val totalReview: Int
) {
    fun toWisata() = Wisata(id, nama, deskripsi, lat, lng, image, kategori, harga, rating, totalReview)
}
