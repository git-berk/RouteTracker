package com.gitberk.routetracker.core.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val timestampMillis: Long,
)
