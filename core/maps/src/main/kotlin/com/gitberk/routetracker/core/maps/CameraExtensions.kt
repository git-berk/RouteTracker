package com.gitberk.routetracker.core.maps

import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState

const val STREET_ZOOM = 16f

val RouteMarker.latLng: LatLng get() = LatLng(latitude, longitude)

val LocationPoint.latLng: LatLng get() = LatLng(latitude, longitude)

// Call only after the map is laid out; bounds updates need the map size.
fun CameraPositionState.showRoute(markers: List<RouteMarker>, paddingPx: Int) {
    when (markers.size) {
        0 -> Unit
        1 -> move(CameraUpdateFactory.newLatLngZoom(markers.single().latLng, STREET_ZOOM))
        else -> {
            val bounds = LatLngBounds.builder()
                .apply { markers.forEach { include(it.latLng) } }
                .build()
            move(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx))
        }
    }
}

suspend fun CameraPositionState.animateTo(target: LatLng, zoom: Float? = null) {
    val update = if (zoom != null) {
        CameraUpdateFactory.newLatLngZoom(target, zoom)
    } else {
        CameraUpdateFactory.newLatLng(target)
    }
    animate(update)
}
