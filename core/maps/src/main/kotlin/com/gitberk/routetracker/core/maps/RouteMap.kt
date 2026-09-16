package com.gitberk.routetracker.core.maps

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gitberk.routetracker.core.model.RouteMarker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

private val RouteLineWidth = 6.dp

@Composable
fun RouteMap(
    markers: List<RouteMarker>,
    cameraPositionState: CameraPositionState,
    isMyLocationEnabled: Boolean,
    onMarkerClick: (RouteMarker) -> Unit,
    onMapLoaded: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        contentPadding = contentPadding,
        properties = MapProperties(isMyLocationEnabled = isMyLocationEnabled),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
        ),
        onMapLoaded = onMapLoaded,
    ) {
        val context = LocalContext.current
        val pinIcon = remember { LocationPinIcon.get(context) }

        if (markers.size > 1) {
            val points = remember(markers) { markers.map(RouteMarker::latLng) }
            Polyline(
                points = points,
                color = MaterialTheme.colorScheme.primary,
                width = with(LocalDensity.current) { RouteLineWidth.toPx() },
                geodesic = true,
            )
        }

        markers.forEach { marker ->
            key(marker.id) {
                Marker(
                    state = rememberUpdatedMarkerState(position = marker.latLng),
                    icon = pinIcon,
                    anchor = LocationPinIcon.anchor,
                    onClick = {
                        onMarkerClick(marker)
                        true
                    },
                )
            }
        }
    }
}
