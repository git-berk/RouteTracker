package com.gitberk.routetracker.core.tracking

interface TrackingController {
    suspend fun start()

    suspend fun stop()

    suspend fun resumeIfNeeded()
}
