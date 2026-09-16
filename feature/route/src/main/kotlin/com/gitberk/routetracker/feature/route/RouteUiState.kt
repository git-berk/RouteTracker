package com.gitberk.routetracker.feature.route

import com.gitberk.routetracker.core.model.RouteMarker

data class RouteUiState(
    val isLoading: Boolean = true,
    val markers: List<RouteMarker> = emptyList(),
    val isTracking: Boolean = false,
    val selectedMarker: SelectedMarker? = null,
)

data class SelectedMarker(
    val marker: RouteMarker,
    /** 1-based position along the route. */
    val number: Int,
    val address: AddressState,
)

sealed interface AddressState {
    data object Loading : AddressState

    data class Loaded(val address: String) : AddressState

    data object NotFound : AddressState

    data object Failed : AddressState
}
