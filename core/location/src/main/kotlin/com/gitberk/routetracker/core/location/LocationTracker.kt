package com.gitberk.routetracker.core.location

import com.gitberk.routetracker.core.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationTracker {
    fun locationUpdates(): Flow<LocationPoint>

    suspend fun currentLocation(): LocationPoint?
}
