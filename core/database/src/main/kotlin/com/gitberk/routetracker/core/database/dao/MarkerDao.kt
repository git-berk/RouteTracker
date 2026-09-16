package com.gitberk.routetracker.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gitberk.routetracker.core.database.model.MarkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarkerDao {

    @Query("SELECT * FROM markers ORDER BY id ASC")
    fun observeMarkers(): Flow<List<MarkerEntity>>

    @Query("SELECT * FROM markers ORDER BY id DESC LIMIT 1")
    suspend fun getLastMarker(): MarkerEntity?

    @Insert
    suspend fun insert(marker: MarkerEntity): Long

    @Query("UPDATE markers SET address = :address WHERE id = :id")
    suspend fun updateAddress(id: Long, address: String)

    @Query("DELETE FROM markers")
    suspend fun deleteAll()
}
