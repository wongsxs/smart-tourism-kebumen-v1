package com.yuudev.wisatakebumen.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.yuudev.wisatakebumen.model.Review

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: Int,
    val wisataId: String,
    val username: String,
    val nama: String,
    val rating: Int,
    val komentar: String,
    val tanggal: String
) {
    fun toReview() = Review(id, wisataId, username, nama, rating, komentar, tanggal)
}
