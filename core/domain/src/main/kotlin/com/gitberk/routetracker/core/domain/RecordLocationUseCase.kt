package com.gitberk.routetracker.core.domain

import com.gitberk.routetracker.core.common.location.distanceInMeters
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.model.LocationPoint
import javax.inject.Inject

const val MARKER_DISTANCE_METERS = 100.0

// A fix this imprecise can be further off than the marker spacing itself, which would
// drop fake markers while the user is standing still.
const val MAX_ACCEPTED_ACCURACY_METERS = 50f

/**
 * Stores [point] as a new marker when the route is empty or the user has moved at least
 * [MARKER_DISTANCE_METERS] from the last marker. Measuring from the last marker rather than the
 * previous fix means slow movement still adds up to a marker.
 */
class RecordLocationUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
) {
    /** Returns true when a marker was added. */
    suspend operator fun invoke(point: LocationPoint): Boolean {
        if (point.accuracyMeters > MAX_ACCEPTED_ACCURACY_METERS) return false

        val lastMarker = routeRepository.getLastMarker()
        if (lastMarker != null) {
            val distance = distanceInMeters(
                fromLatitude = lastMarker.latitude,
                fromLongitude = lastMarker.longitude,
                toLatitude = point.latitude,
                toLongitude = point.longitude,
            )
            if (distance < MARKER_DISTANCE_METERS) return false
        }

        routeRepository.addMarker(point)
        return true
    }
}
