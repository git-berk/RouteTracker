package com.gitberk.routetracker.core.common.location

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_008.8

fun distanceInMeters(
    fromLatitude: Double,
    fromLongitude: Double,
    toLatitude: Double,
    toLongitude: Double,
): Double {
    val deltaLatitude = Math.toRadians(toLatitude - fromLatitude)
    val deltaLongitude = Math.toRadians(toLongitude - fromLongitude)
    val a = sin(deltaLatitude / 2).pow(2) +
        cos(Math.toRadians(fromLatitude)) * cos(Math.toRadians(toLatitude)) * sin(deltaLongitude / 2).pow(2)
    return 2 * EARTH_RADIUS_METERS * asin(sqrt(a))
}
