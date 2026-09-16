package com.gitberk.routetracker.core.domain

import com.gitberk.routetracker.core.common.location.distanceInMeters
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import javax.inject.Inject

const val MARKER_DISTANCE_METERS = 100.0

// A fix this imprecise can be further off than the marker spacing itself, which would
// drop fake markers while the user is standing still.
const val MAX_ACCEPTED_ACCURACY_METERS = 50f

// Binary search steps for the crossing point; 30 halvings of a segment of a few hundred meters
// lands well under a millimeter.
private const val SEARCH_ITERATIONS = 30

/**
 * Places a marker every [MARKER_DISTANCE_METERS] along the user's path.
 *
 * Fixes arrive every few seconds, so the user is usually already past the 100 m mark when one
 * comes in. Instead of dropping the marker at that fix, it is placed where the segment from the
 * previous fix to the new one is exactly 100 m from the last marker. A long segment can hold
 * several markers. This keeps the spacing exact regardless of speed or update interval.
 *
 * Holds the previous fix in memory, so one instance should serve one tracking session.
 */
class RecordLocationUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
) {
    private var previousFix: LocationPoint? = null

    /** Returns the number of markers added. */
    suspend operator fun invoke(fix: LocationPoint): Int {
        if (fix.accuracyMeters > MAX_ACCEPTED_ACCURACY_METERS) return 0

        val lastMarker = routeRepository.getLastMarker()
        if (lastMarker == null) {
            routeRepository.addMarker(fix)
            previousFix = fix
            return 1
        }

        var anchor = lastMarker.asLocationPoint()
        // Without a usable previous fix (new session, or the route was just reset), the segment
        // starts at the last marker itself.
        var segmentStart = previousFix
            ?.takeIf { it.distanceTo(anchor) < MARKER_DISTANCE_METERS }
            ?: anchor

        var added = 0
        while (fix.distanceTo(anchor) >= MARKER_DISTANCE_METERS) {
            val marker = pointAtDistanceFrom(anchor, segmentStart, fix, MARKER_DISTANCE_METERS)
            routeRepository.addMarker(marker)
            anchor = marker
            segmentStart = marker
            added++
        }
        previousFix = fix
        return added
    }

    /**
     * Finds the point between [start] and [end] that is [distance] meters from [anchor], given that
     * [start] is closer than that and [end] is at least that far. Coordinates are interpolated
     * linearly, which is accurate at the scale of a few hundred meters.
     */
    private fun pointAtDistanceFrom(
        anchor: LocationPoint,
        start: LocationPoint,
        end: LocationPoint,
        distance: Double,
    ): LocationPoint {
        var low = 0.0
        var high = 1.0
        repeat(SEARCH_ITERATIONS) {
            val middle = (low + high) / 2
            if (interpolate(start, end, middle).distanceTo(anchor) < distance) low = middle else high = middle
        }
        return interpolate(start, end, high)
    }

    private fun interpolate(start: LocationPoint, end: LocationPoint, fraction: Double) = LocationPoint(
        latitude = start.latitude + (end.latitude - start.latitude) * fraction,
        longitude = start.longitude + (end.longitude - start.longitude) * fraction,
        accuracyMeters = end.accuracyMeters,
        timestampMillis = start.timestampMillis + ((end.timestampMillis - start.timestampMillis) * fraction).toLong(),
    )
}

private fun LocationPoint.distanceTo(other: LocationPoint) =
    distanceInMeters(latitude, longitude, other.latitude, other.longitude)

private fun RouteMarker.asLocationPoint() = LocationPoint(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = 0f,
    timestampMillis = recordedAtMillis,
)
