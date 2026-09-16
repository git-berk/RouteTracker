package com.gitberk.routetracker.core.domain

import com.gitberk.routetracker.core.common.location.distanceInMeters
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import com.gitberk.routetracker.core.testing.repository.FakeRouteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

// Roughly 1 m of latitude (and of longitude at the equator), so offsets below read as meters.
private const val DEGREES_PER_METER = 1.0 / 111_195

private const val TOLERANCE_METERS = 0.01

class RecordLocationUseCaseTest {

    private val repository = FakeRouteRepository()
    private val recordLocation = RecordLocationUseCase(repository)

    @Test
    fun firstAccurateFix_becomesStartMarker() = runTest {
        assertEquals(1, recordLocation(fix(north = 0.0)))
        assertEquals(listOf(0.0), markersNorth())
    }

    @Test
    fun fixCloserThanMarkerDistance_addsNothing() = runTest {
        recordLocation(fix(north = 0.0))

        assertEquals(0, recordLocation(fix(north = 99.0)))
        assertEquals(1, repository.currentMarkers.size)
    }

    @Test
    fun fixPastMarkerDistance_placesMarkerAtExactlyOneHundredMeters() = runTest {
        recordLocation(fix(north = 0.0))

        assertEquals(1, recordLocation(fix(north = 130.0)))
        assertMarkersNorth(0.0, 100.0)
    }

    @Test
    fun longSegment_placesOneMarkerPerHundredMeters() = runTest {
        recordLocation(fix(north = 0.0))

        assertEquals(3, recordLocation(fix(north = 350.0)))
        assertMarkersNorth(0.0, 100.0, 200.0, 300.0)
    }

    @Test
    fun spacingContinuesFromPlacedMarker_notFromFix() = runTest {
        recordLocation(fix(north = 0.0))
        recordLocation(fix(north = 60.0))
        recordLocation(fix(north = 130.0))

        assertEquals(1, recordLocation(fix(north = 215.0)))
        assertMarkersNorth(0.0, 100.0, 200.0)
    }

    @Test
    fun turningPath_keepsMarkersOneHundredMetersApart() = runTest {
        recordLocation(fix(north = 0.0))
        recordLocation(fix(north = 80.0))
        // Markers land at (80 N, 60 E) and (80 N, 160 E): 100 m straight-line from the previous one.
        recordLocation(fix(north = 80.0, east = 250.0))

        val markers = repository.currentMarkers
        assertEquals(3, markers.size)
        markers.zipWithNext { a, b -> assertEquals(100.0, a.distanceTo(b), TOLERANCE_METERS) }
    }

    @Test
    fun markerTime_isInterpolatedBetweenFixes() = runTest {
        recordLocation(fix(north = 0.0, timestampMillis = 0L))
        recordLocation(fix(north = 50.0, timestampMillis = 10_000L))

        recordLocation(fix(north = 150.0, timestampMillis = 20_000L))

        assertEquals(15_000.0, repository.currentMarkers.last().recordedAtMillis.toDouble(), 5.0)
    }

    @Test
    fun inaccurateFix_isIgnored() = runTest {
        assertEquals(0, recordLocation(fix(north = 0.0, accuracyMeters = 80f)))
        assertEquals(0, repository.currentMarkers.size)
    }

    @Test
    fun afterReset_nextFixBecomesNewStartMarker() = runTest {
        recordLocation(fix(north = 0.0))
        recordLocation(fix(north = 50.0))
        repository.clearRoute()

        assertEquals(1, recordLocation(fix(north = 500.0)))
        assertMarkersNorth(500.0)
    }

    private fun fix(
        north: Double,
        east: Double = 0.0,
        accuracyMeters: Float = 5f,
        timestampMillis: Long = 0L,
    ) = LocationPoint(
        latitude = north * DEGREES_PER_METER,
        longitude = east * DEGREES_PER_METER,
        accuracyMeters = accuracyMeters,
        timestampMillis = timestampMillis,
    )

    private fun markersNorth() = repository.currentMarkers.map { it.latitude / DEGREES_PER_METER }

    private fun assertMarkersNorth(vararg expected: Double) {
        val actual = markersNorth()
        assertEquals(expected.size, actual.size)
        expected.zip(actual).forEach { (e, a) -> assertEquals(e, a, TOLERANCE_METERS) }
    }

    private fun RouteMarker.distanceTo(other: RouteMarker) =
        distanceInMeters(latitude, longitude, other.latitude, other.longitude)
}
