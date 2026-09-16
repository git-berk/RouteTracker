package com.gitberk.routetracker.core.model

data class RouteMarker(
    val id: Long,
    val latitude: Double,
    val longitude: Double,
    val recordedAtMillis: Long,
    val address: String?,
)
