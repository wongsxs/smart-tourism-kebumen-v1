package com.yuudev.wisatakebumen.repository

import android.content.Context
import com.yuudev.wisatakebumen.data.DatabaseProvider
import com.yuudev.wisatakebumen.data.WisataEntity
import com.yuudev.wisatakebumen.model.Wisata
import com.yuudev.wisatakebumen.network.RetrofitClient
import com.yuudev.wisatakebumen.util.CachePolicy
import com.yuudev.wisatakebumen.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WisataRepository(private val context: Context) {
    private val dao = DatabaseProvider.getDatabase(context).wisataDao()
    private val networkMonitor = NetworkMonitor(context)

    fun getWisataStream(): Flow<List<Wisata>> {
        return dao.getWisataStream().map { entities -> 
            entities.map { it.toWisata() }
        }
    }

    suspend fun refreshWisata(force: Boolean = false) {
        val prefs = context.getSharedPreferences("cache_prefs", Context.MODE_PRIVATE)
        val lastSync = prefs.getLong("last_wisata_sync", 0L)
        val now = System.currentTimeMillis()

        if (force || now - lastSync > CachePolicy.WISATA_CACHE_DURATION) {
            if (networkMonitor.isNetworkAvailable()) {
                val wisataList = RetrofitClient.api.getWisata()
                val entities = wisataList.map { w ->
                    WisataEntity(w.id, w.nama, w.deskripsi, w.lat, w.lng, w.image, w.kategori, w.harga, w.rating, w.totalReview)
                }
                dao.deleteAllWisata()
                dao.insertWisataList(entities)
                prefs.edit().putLong("last_wisata_sync", now).apply()
            } else if (force) {
                throw Exception("Tidak ada koneksi internet")
            }
        }
    }
}
