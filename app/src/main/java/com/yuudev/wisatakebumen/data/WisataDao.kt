package com.yuudev.wisatakebumen.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WisataDao {
    @Query("SELECT * FROM wisata")
    fun getWisataStream(): Flow<List<WisataEntity>>

    @Query("SELECT * FROM wisata")
    suspend fun getWisataList(): List<WisataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWisataList(wisataList: List<WisataEntity>)

    @Query("DELETE FROM wisata")
    suspend fun deleteAllWisata()
}
