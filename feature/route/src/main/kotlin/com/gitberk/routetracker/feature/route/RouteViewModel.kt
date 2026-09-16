package com.gitberk.routetracker.feature.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import com.gitberk.routetracker.core.location.LocationTracker
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.tracking.TrackingController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    trackingStateRepository: TrackingStateRepository,
    private val trackingController: TrackingController,
    private val locationTracker: LocationTracker,
) : ViewModel() {

    val uiState: StateFlow<RouteUiState> = combine(
        routeRepository.markers,
        trackingStateRepository.isTracking,
    ) { markers, isTracking ->
        RouteUiState(isLoading = false, markers = markers, isTracking = isTracking)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RouteUiState(),
    )

    fun startTracking() {
        viewModelScope.launch { trackingController.start() }
    }

    fun stopTracking() {
        viewModelScope.launch { trackingController.stop() }
    }

    /** Tracking keeps running if it was on; the next accurate fix becomes the new starting marker. */
    fun resetRoute() {
        viewModelScope.launch { routeRepository.clearRoute() }
    }

    suspend fun currentLocation(): LocationPoint? = try {
        locationTracker.currentLocation()
    } catch (e: SecurityException) {
        null
    }
}
