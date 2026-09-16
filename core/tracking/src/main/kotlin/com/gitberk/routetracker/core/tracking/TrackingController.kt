package com.gitberk.routetracker.core.tracking

interface TrackingController {

    /** Starts recording. Callers must make sure precise location permission is granted. */
    suspend fun start()

    suspend fun stop()

    /**
     * Restarts the service when tracking was left on but the service is gone, e.g. the system
     * killed the process. Safe to call while the service is already running.
     */
    suspend fun resumeIfNeeded()
}
