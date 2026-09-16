package com.gitberk.routetracker.feature.route

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitberk.routetracker.core.data.repository.AddressRepository
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import com.gitberk.routetracker.core.location.LocationTracker
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import com.gitberk.routetracker.core.tracking.TrackingController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository,
    trackingStateRepository: TrackingStateRepository,
    private val addressRepository: AddressRepository,
    private val trackingController: TrackingController,
    private val locationTracker: LocationTracker,
) : ViewModel() {
    private val selectedMarkerId = MutableStateFlow<Long?>(null)
    private val addressState = MutableStateFlow<AddressState>(AddressState.Loading)
    private var addressJob: Job? = null

    val uiState: StateFlow<RouteUiState> = combine(
        routeRepository.markers,
        trackingStateRepository.isTracking,
        selectedMarkerId,
        addressState,
    ) { markers, isTracking, selectedId, address ->
        val selectedIndex = markers.indexOfFirst { it.id == selectedId }
        RouteUiState(
            isLoading = false,
            markers = markers,
            isTracking = isTracking,
            selectedMarker = markers.getOrNull(selectedIndex)?.let { marker ->
                SelectedMarker(marker = marker, number = selectedIndex + 1, address = address)
            },
        )
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

    fun resetRoute() {
        viewModelScope.launch { routeRepository.clearRoute() }
    }

    fun selectMarker(marker: RouteMarker) {
        selectedMarkerId.value = marker.id
        loadAddress(marker)
    }

    fun retryAddress() {
        uiState.value.selectedMarker?.let { loadAddress(it.marker) }
    }

    fun dismissMarker() {
        addressJob?.cancel()
        selectedMarkerId.value = null
    }

    suspend fun currentLocation(): LocationPoint? = try {
        locationTracker.currentLocation()
    } catch (e: SecurityException) {
        null
    }

    private fun loadAddress(marker: RouteMarker) {
        addressJob?.cancel()
        addressState.value = AddressState.Loading
        addressJob = viewModelScope.launch {
            addressState.value = try {
                addressRepository.getAddress(marker)?.let(AddressState::Loaded) ?: AddressState.NotFound
            } catch (e: IOException) {
                AddressState.Failed
            }
        }
    }
}
