package com.gitberk.routetracker.core.testing.location

import com.gitberk.routetracker.core.location.LocationTracker
import com.gitberk.routetracker.core.model.LocationPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeLocationTracker : LocationTracker {

    private val updates = MutableSharedFlow<LocationPoint>()

    var currentLocation: LocationPoint? = null

    suspend fun emit(point: LocationPoint) = updates.emit(point)

    override fun locationUpdates(): Flow<LocationPoint> = updates

    override suspend fun currentLocation(): LocationPoint? = currentLocation
}
