package com.gitberk.routetracker.core.testing.repository

import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTrackingStateRepository(isTracking: Boolean = false) : TrackingStateRepository {

    override val isTracking = MutableStateFlow(isTracking)

    override suspend fun setTracking(isTracking: Boolean) {
        this.isTracking.value = isTracking
    }
}
