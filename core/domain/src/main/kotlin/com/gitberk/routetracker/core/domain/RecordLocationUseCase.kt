package com.gitberk.routetracker.core.domain

import com.gitberk.routetracker.core.common.location.distanceInMeters
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import javax.inject.Inject

const val MARKER_DISTANCE_METERS = 100.0

const val MAX_ACCEPTED_ACCURACY_METERS = 50f

private const val SEARCH_ITERATIONS = 30

/**
 * Places markers exactly [MARKER_DISTANCE_METERS] apart: when a fix passes the mark, the marker goes
 * where the segment from the previous fix crosses it, so spacing doesn't depend on the update rate.
 * Keeps the previous fix in memory, so use one instance per tracking session.
 */
class RecordLocationUseCase @Inject constructor(
    private val routeRepository: RouteRepository,
) {
    private var previousFix: LocationPoint? = null

    suspend operator fun invoke(fix: LocationPoint): Int {
        if (fix.accuracyMeters > MAX_ACCEPTED_ACCURACY_METERS) return 0

        val lastMarker = routeRepository.getLastMarker()?.asLocationPoint()
        val previous = previousFix
        previousFix = fix

        if (lastMarker == null) {
            routeRepository.addMarker(fix)
            return 1
        }
        if (previous == null) {
            if (fix.distanceTo(lastMarker) < MARKER_DISTANCE_METERS) return 0
            routeRepository.addMarker(fix)
            return 1
        }

        var anchor: LocationPoint = lastMarker
        var segmentStart: LocationPoint = previous
        var added = 0
        while (fix.distanceTo(anchor) >= MARKER_DISTANCE_METERS) {
            val marker = pointAtDistanceFrom(anchor, segmentStart, fix, MARKER_DISTANCE_METERS)
            routeRepository.addMarker(marker)
            anchor = marker
            segmentStart = marker
            added++
        }
        return added
    }

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
