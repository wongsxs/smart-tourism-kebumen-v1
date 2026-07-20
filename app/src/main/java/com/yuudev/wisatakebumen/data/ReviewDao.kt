package com.yuudev.wisatakebumen.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Query("SELECT * FROM reviews WHERE wisataId = :wisataId")
    fun getReviewsStream(wisataId: String): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE wisataId = :wisataId")
    suspend fun getReviews(wisataId: String): List<ReviewEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)

    @Query("DELETE FROM reviews WHERE wisataId = :wisataId")
    suspend fun deleteReviewsByWisataId(wisataId: String)
}
