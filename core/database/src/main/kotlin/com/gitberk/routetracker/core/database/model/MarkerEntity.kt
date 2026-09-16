package com.gitberk.routetracker.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gitberk.routetracker.core.model.RouteMarker

@Entity(tableName = "markers")
data class MarkerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    // Filled lazily the first time the marker is opened, so geocoding happens at most once per marker.
    val address: String? = null,
)

fun MarkerEntity.asExternalModel() = RouteMarker(
    id = id,
    latitude = latitude,
    longitude = longitude,
    recordedAtMillis = recordedAtMillis,
    address = address,
)
