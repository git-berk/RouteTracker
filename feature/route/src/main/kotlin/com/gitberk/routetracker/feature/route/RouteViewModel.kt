package com.gitberk.routetracker.feature.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    routeRepository: RouteRepository,
    trackingStateRepository: TrackingStateRepository,
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
}
