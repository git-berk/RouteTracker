package com.gitberk.routetracker.core.testing.tracking

import com.gitberk.routetracker.core.testing.repository.FakeTrackingStateRepository
import com.gitberk.routetracker.core.tracking.TrackingController

class FakeTrackingController(
    private val trackingStateRepository: FakeTrackingStateRepository,
) : TrackingController {

    override suspend fun start() = trackingStateRepository.setTracking(true)

    override suspend fun stop() = trackingStateRepository.setTracking(false)

    override suspend fun resumeIfNeeded() = Unit
}
