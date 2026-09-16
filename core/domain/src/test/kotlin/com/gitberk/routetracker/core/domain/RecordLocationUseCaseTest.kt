package com.gitberk.routetracker.core.domain

import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.testing.repository.FakeRouteRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// Roughly 1 m of latitude, so offsets below read as meters.
private const val DEGREES_PER_METER = 1.0 / 111_195

class RecordLocationUseCaseTest {

    private val repository = FakeRouteRepository()
    private val recordLocation = RecordLocationUseCase(repository)

    @Test
    fun firstAccurateFix_becomesStartMarker() = runTest {
        assertTrue(recordLocation(pointMetersNorth(0.0)))
        assertEquals(1, repository.currentMarkers.size)
    }

    @Test
    fun fixCloserThanMarkerDistance_isIgnored() = runTest {
        recordLocation(pointMetersNorth(0.0))

        assertFalse(recordLocation(pointMetersNorth(99.0)))
        assertEquals(1, repository.currentMarkers.size)
    }

    @Test
    fun fixAtMarkerDistance_addsMarker() = runTest {
        recordLocation(pointMetersNorth(0.0))

        assertTrue(recordLocation(pointMetersNorth(101.0)))
        assertEquals(2, repository.currentMarkers.size)
    }

    @Test
    fun distanceIsMeasuredFromLastMarker_notPreviousFix() = runTest {
        recordLocation(pointMetersNorth(0.0))
        recordLocation(pointMetersNorth(60.0))

        assertTrue(recordLocation(pointMetersNorth(120.0)))
        assertEquals(120.0, repository.currentMarkers.last().latitude / DEGREES_PER_METER, 0.5)
    }

    @Test
    fun inaccurateFix_isIgnored() = runTest {
        assertFalse(recordLocation(pointMetersNorth(0.0, accuracyMeters = 80f)))
        assertTrue(repository.currentMarkers.isEmpty())
    }

    private fun pointMetersNorth(meters: Double, accuracyMeters: Float = 5f) = LocationPoint(
        latitude = meters * DEGREES_PER_METER,
        longitude = 0.0,
        accuracyMeters = accuracyMeters,
        timestampMillis = 0L,
    )
}
