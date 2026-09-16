package com.gitberk.routetracker.core.location

import com.gitberk.routetracker.core.model.LocationPoint
import kotlinx.coroutines.flow.Flow

interface LocationTracker {

    /** Emits location fixes until collection is cancelled. Fails if location permission is missing. */
    fun locationUpdates(): Flow<LocationPoint>

    suspend fun currentLocation(): LocationPoint?
}
