package com.gitberk.routetracker.feature.route

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.gitberk.routetracker.core.location.hasLocationPermission

internal val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/** Notifications are asked together with location so the tracking notification is visible from the start. */
internal val TrackingPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    LocationPermissions + Manifest.permission.POST_NOTIFICATIONS
} else {
    LocationPermissions
}

/**
 * Runs an action once precise location is granted, asking for any missing permissions first.
 * A denied notification permission doesn't block the action; tracking works without it.
 */
@Stable
internal class PermissionGate(private val context: Context) {

    var hasLocationPermission by mutableStateOf(context.hasLocationPermission())
        private set

    var showRationale by mutableStateOf(false)
        private set

    internal var launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>? = null

    private var pendingAction: (() -> Unit)? = null

    fun runWithPermissions(permissions: Array<String>, action: () -> Unit) {
        val missing = missingPermissions(permissions)
        if (missing.isEmpty()) {
            action()
            return
        }
        pendingAction = action
        launcher?.launch(missing)
    }

    /** Asks for missing permissions without a follow-up action, so a denial doesn't show the rationale. */
    fun requestMissing(permissions: Array<String>) {
        val missing = missingPermissions(permissions)
        if (missing.isNotEmpty()) launcher?.launch(missing)
    }

    fun dismissRationale() {
        showRationale = false
    }

    internal fun onPermissionResult() {
        refresh()
        val action = pendingAction ?: return
        pendingAction = null
        if (hasLocationPermission) action() else showRationale = true
    }

    internal fun refresh() {
        hasLocationPermission = context.hasLocationPermission()
    }

    private fun missingPermissions(permissions: Array<String>): Array<String> =
        permissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
}

@Composable
internal fun rememberPermissionGate(): PermissionGate {
    val context = LocalContext.current
    val gate = remember(context) { PermissionGate(context) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        gate.onPermissionResult()
    }
    SideEffect { gate.launcher = launcher }
    // The user may grant or revoke permissions from system settings while the app is in the background.
    LifecycleResumeEffect(gate) {
        gate.refresh()
        onPauseOrDispose {}
    }
    return gate
}
