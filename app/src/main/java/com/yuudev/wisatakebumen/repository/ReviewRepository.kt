package com.yuudev.wisatakebumen.repository

import android.content.Context
import com.yuudev.wisatakebumen.data.DatabaseProvider
import com.yuudev.wisatakebumen.data.ReviewEntity
import com.yuudev.wisatakebumen.model.Review
import com.yuudev.wisatakebumen.network.RetrofitClient
import com.yuudev.wisatakebumen.util.CachePolicy
import com.yuudev.wisatakebumen.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReviewRepository(private val context: Context) {
    private val dao = DatabaseProvider.getDatabase(context).reviewDao()
    private val networkMonitor = NetworkMonitor(context)

    fun getReviewsStream(wisataId: String): Flow<List<Review>> {
        return dao.getReviewsStream(wisataId).map { entities -> 
            entities.map { it.toReview() }
        }
    }

    suspend fun refreshReviews(wisataId: String, force: Boolean = false) {
        val prefs = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)
        val key = "last_review_sync_"
        val lastSync = prefs.getLong(key, 0L)
        val now = System.currentTimeMillis()

        if (force || now - lastSync > CachePolicy.REVIEW_CACHE_DURATION) {
            if (networkMonitor.isNetworkAvailable()) {
                val response = RetrofitClient.api.getReview(wisataId = wisataId)
                val entities = response.reviews.map { r ->
                    ReviewEntity(r.id, r.wisataId, r.username, r.nama, r.rating, r.komentar, r.tanggal)
                }
                dao.deleteReviewsByWisataId(wisataId)
                dao.insertReviews(entities)
                prefs.edit().putLong(key, now).apply()
            } else if (force) {
                throw Exception("Tidak ada koneksi internet")
            }
        }
    }
}
