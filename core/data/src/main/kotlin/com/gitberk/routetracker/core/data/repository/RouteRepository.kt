package com.gitberk.routetracker.core.data.repository

import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import kotlinx.coroutines.flow.Flow

interface RouteRepository {

    /** Markers in the order they were recorded. */
    val markers: Flow<List<RouteMarker>>

    suspend fun getLastMarker(): RouteMarker?

    suspend fun addMarker(point: LocationPoint)

    suspend fun clearRoute()
}
