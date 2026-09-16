package com.gitberk.routetracker.core.testing.repository

import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeRouteRepository(initialMarkers: List<RouteMarker> = emptyList()) : RouteRepository {

    private val markersFlow = MutableStateFlow(initialMarkers)

    override val markers: Flow<List<RouteMarker>> = markersFlow

    val currentMarkers: List<RouteMarker> get() = markersFlow.value

    override suspend fun getLastMarker(): RouteMarker? = markersFlow.value.lastOrNull()

    override suspend fun addMarker(point: LocationPoint) {
        markersFlow.update { markers ->
            markers + RouteMarker(
                id = (markers.maxOfOrNull { it.id } ?: 0) + 1,
                latitude = point.latitude,
                longitude = point.longitude,
                recordedAtMillis = point.timestampMillis,
                address = null,
            )
        }
    }

    override suspend fun clearRoute() {
        markersFlow.value = emptyList()
    }
}
