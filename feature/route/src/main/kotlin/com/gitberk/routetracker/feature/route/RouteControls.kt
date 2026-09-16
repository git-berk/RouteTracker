package com.gitberk.routetracker.feature.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gitberk.routetracker.core.designsystem.component.MapActionButton
import com.gitberk.routetracker.core.designsystem.component.MapIconButton
import com.gitberk.routetracker.core.designsystem.icon.RouteTrackerIcons

@Composable
internal fun RouteStatusChip(
    isTracking: Boolean,
    markerCount: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
            )
            Text(
                text = stringResource(
                    R.string.route_status_format,
                    stringResource(if (isTracking) R.string.route_status_tracking else R.string.route_status_idle),
                    pluralStringResource(R.plurals.route_marker_count, markerCount, markerCount),
                ),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun RouteControls(
    isTracking: Boolean,
    canReset: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onResetClick: () -> Unit,
    onMyLocationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MapIconButton(
            icon = RouteTrackerIcons.MyLocation,
            contentDescription = stringResource(R.string.route_show_my_location),
            onClick = onMyLocationClick,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (canReset) {
                MapActionButton(
                    text = stringResource(R.string.route_reset),
                    icon = RouteTrackerIcons.Delete,
                    onClick = onResetClick,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.error,
                )
            } else {
                Spacer(Modifier)
            }
            if (isTracking) {
                MapActionButton(
                    text = stringResource(R.string.route_stop_tracking),
                    icon = RouteTrackerIcons.Stop,
                    onClick = onStopClick,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
            } else {
                MapActionButton(
                    text = stringResource(R.string.route_start_tracking),
                    icon = RouteTrackerIcons.Play,
                    onClick = onStartClick,
                )
            }
        }
    }
}

@Composable
internal fun ResetRouteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.route_reset_dialog_title)) },
        text = { Text(stringResource(R.string.route_reset_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.route_reset_dialog_confirm),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.route_dialog_cancel)) }
        },
    )
}

@Composable
internal fun LocationPermissionDialog(
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.route_permission_dialog_title)) },
        text = { Text(stringResource(R.string.route_permission_dialog_message)) },
        confirmButton = {
            TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.route_permission_dialog_open_settings)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.route_dialog_cancel)) }
        },
    )
}
