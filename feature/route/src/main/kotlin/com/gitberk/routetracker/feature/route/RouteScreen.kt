package com.gitberk.routetracker.feature.route

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gitberk.routetracker.core.maps.RouteMap
import com.gitberk.routetracker.core.maps.STREET_ZOOM
import com.gitberk.routetracker.core.maps.animateTo
import com.gitberk.routetracker.core.maps.latLng
import com.gitberk.routetracker.core.maps.showRoute
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

private val RouteFramePadding = 64.dp

private const val FOLLOW_MIN_ZOOM = 12f

private val ControlsHeight = 88.dp

@Composable
fun RouteScreen(
    modifier: Modifier = Modifier,
    viewModel: RouteViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionGate = rememberPermissionGate()
    val context = LocalContext.current

    var hasRequestedPermissions by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!hasRequestedPermissions) {
            hasRequestedPermissions = true
            permissionGate.requestMissing(TrackingPermissions)
        }
    }

    RouteScreen(
        uiState = uiState,
        hasLocationPermission = permissionGate.hasLocationPermission,
        onStartClick = { permissionGate.runWithPermissions(TrackingPermissions, viewModel::startTracking) },
        onStopClick = viewModel::stopTracking,
        onResetConfirm = viewModel::resetRoute,
        onMarkerClick = viewModel::selectMarker,
        onRetryAddress = viewModel::retryAddress,
        onDismissMarker = viewModel::dismissMarker,
        onMyLocationClick = { onLocated -> permissionGate.runWithPermissions(LocationPermissions, onLocated) },
        requestCurrentLocation = viewModel::currentLocation,
        modifier = modifier,
    )

    if (permissionGate.showRationale) {
        LocationPermissionDialog(
            onOpenSettings = {
                permissionGate.dismissRationale()
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
            onDismiss = permissionGate::dismissRationale,
        )
    }
}

@Composable
internal fun RouteScreen(
    uiState: RouteUiState,
    hasLocationPermission: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onResetConfirm: () -> Unit,
    onMarkerClick: (RouteMarker) -> Unit,
    onRetryAddress: () -> Unit,
    onDismissMarker: () -> Unit,
    onMyLocationClick: (onPermissionGranted: () -> Unit) -> Unit,
    requestCurrentLocation: suspend () -> LocationPoint?,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState()
    var isMapLoaded by remember { mutableStateOf(false) }
    var hasPositionedCamera by rememberSaveable { mutableStateOf(false) }
    var showResetDialog by rememberSaveable { mutableStateOf(false) }
    val framePaddingPx = with(LocalDensity.current) { RouteFramePadding.roundToPx() }

    LaunchedEffect(isMapLoaded, uiState.isLoading, hasLocationPermission) {
        if (!isMapLoaded || uiState.isLoading || hasPositionedCamera) return@LaunchedEffect
        if (uiState.markers.isNotEmpty()) {
            cameraPositionState.showRoute(uiState.markers, framePaddingPx)
            hasPositionedCamera = true
        } else if (hasLocationPermission) {
            requestCurrentLocation()?.let { location ->
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location.latLng, STREET_ZOOM))
                hasPositionedCamera = true
            }
        }
    }

    val lastMarker = uiState.markers.lastOrNull()
    LaunchedEffect(lastMarker?.id) {
        if (hasPositionedCamera && uiState.isTracking && lastMarker != null) {
            val zoom = if (cameraPositionState.position.zoom < FOLLOW_MIN_ZOOM) STREET_ZOOM else null
            cameraPositionState.animateTo(lastMarker.latLng, zoom)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        RouteMap(
            markers = uiState.markers,
            cameraPositionState = cameraPositionState,
            isMyLocationEnabled = hasLocationPermission,
            onMarkerClick = onMarkerClick,
            onMapLoaded = { isMapLoaded = true },
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.systemBars
                .add(WindowInsets(bottom = ControlsHeight))
                .asPaddingValues(),
        )

        RouteStatusChip(
            isTracking = uiState.isTracking,
            markerCount = uiState.markers.size,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        )

        RouteControls(
            isTracking = uiState.isTracking,
            canReset = uiState.markers.isNotEmpty(),
            onStartClick = onStartClick,
            onStopClick = onStopClick,
            onResetClick = { showResetDialog = true },
            onMyLocationClick = {
                onMyLocationClick {
                    scope.launch {
                        requestCurrentLocation()?.let { location ->
                            cameraPositionState.animateTo(location.latLng, STREET_ZOOM)
                        }
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
        )
    }

    uiState.selectedMarker?.let { selectedMarker ->
        MarkerDetailsSheet(
            selectedMarker = selectedMarker,
            onRetryAddress = onRetryAddress,
            onDismiss = onDismissMarker,
        )
    }

    if (showResetDialog) {
        ResetRouteDialog(
            onConfirm = {
                showResetDialog = false
                onResetConfirm()
            },
            onDismiss = { showResetDialog = false },
        )
    }
}
