package com.gitberk.routetracker.core.common.location

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceTest {

    @Test
    fun samePoint_isZero() {
        assertEquals(0.0, distanceInMeters(41.0082, 28.9784, 41.0082, 28.9784), 0.0)
    }

    @Test
    fun oneThousandthDegreeOfLatitude_isAboutOneHundredElevenMeters() {
        assertEquals(111.2, distanceInMeters(41.0, 29.0, 41.001, 29.0), 0.1)
    }

    @Test
    fun istanbulToAnkara_matchesKnownDistance() {
        val meters = distanceInMeters(41.0082, 28.9784, 39.9334, 32.8597)
        assertEquals(350_000.0, meters, 2_000.0)
    }
}
