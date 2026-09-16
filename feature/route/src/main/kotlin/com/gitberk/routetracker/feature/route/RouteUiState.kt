package com.gitberk.routetracker.feature.route

import com.gitberk.routetracker.core.model.RouteMarker

data class RouteUiState(
    val isLoading: Boolean = true,
    val markers: List<RouteMarker> = emptyList(),
    val isTracking: Boolean = false,
)
