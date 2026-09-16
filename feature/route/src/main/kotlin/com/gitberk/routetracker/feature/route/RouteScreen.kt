package com.gitberk.routetracker.feature.route

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitberk.routetracker.core.maps.RouteMap
import com.gitberk.routetracker.core.maps.showRoute
import com.google.maps.android.compose.rememberCameraPositionState

private val RouteFramePadding = 64.dp

@Composable
fun RouteScreen(
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    RouteScreen(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
internal fun RouteScreen(
    uiState: RouteUiState,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState()
    var isMapLoaded by remember { mutableStateOf(false) }
    // Saved so a rotation doesn't snap the camera back after the user has panned around.
    var hasFramedRoute by rememberSaveable { mutableStateOf(false) }
    val framePaddingPx = with(LocalDensity.current) { RouteFramePadding.roundToPx() }

    LaunchedEffect(isMapLoaded, uiState.isLoading) {
        if (!isMapLoaded || uiState.isLoading || hasFramedRoute) return@LaunchedEffect
        cameraPositionState.showRoute(uiState.markers, framePaddingPx)
        hasFramedRoute = true
    }

    Box(modifier = modifier.fillMaxSize()) {
        RouteMap(
            markers = uiState.markers,
            cameraPositionState = cameraPositionState,
            isMyLocationEnabled = false,
            onMarkerClick = {},
            onMapLoaded = { isMapLoaded = true },
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
        )
    }
}
