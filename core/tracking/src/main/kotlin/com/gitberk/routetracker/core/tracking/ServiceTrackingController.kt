package com.gitberk.routetracker.core.tracking

import android.content.Context
import androidx.core.content.ContextCompat
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import com.gitberk.routetracker.core.location.hasLocationPermission
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal class ServiceTrackingController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val trackingStateRepository: TrackingStateRepository,
) : TrackingController {
    override suspend fun start() {
        trackingStateRepository.setTracking(true)
        startService()
    }

    override suspend fun stop() {
        trackingStateRepository.setTracking(false)
        context.stopService(LocationTrackingService.intent(context))
    }

    override suspend fun resumeIfNeeded() {
        if (!trackingStateRepository.isTracking.first()) return

        if (context.hasLocationPermission()) {
            startService()
        } else {
            trackingStateRepository.setTracking(false)
        }
    }

    private fun startService() {
        ContextCompat.startForegroundService(context, LocationTrackingService.intent(context))
    }
}
