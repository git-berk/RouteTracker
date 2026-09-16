package com.gitberk.routetracker.feature.route

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gitberk.routetracker.core.designsystem.icon.RouteTrackerIcons
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MarkerDetailsSheet(
    selectedMarker: SelectedMarker,
    onRetryAddress: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.route_marker_title, selectedMarker.number),
                style = MaterialTheme.typography.titleLarge,
            )
            DetailRow(icon = RouteTrackerIcons.LocationOn) {
                AddressContent(address = selectedMarker.address, onRetry = onRetryAddress)
                Text(
                    text = remember(selectedMarker.marker) {
                        String.format(Locale.US, "%.5f, %.5f", selectedMarker.marker.latitude, selectedMarker.marker.longitude)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DetailRow(icon = RouteTrackerIcons.Schedule) {
                Text(
                    text = remember(selectedMarker.marker.recordedAtMillis) {
                        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                            .format(Date(selectedMarker.marker.recordedAtMillis))
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
private fun AddressContent(address: AddressState, onRetry: () -> Unit) {
    when (address) {
        AddressState.Loading -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Text(stringResource(R.string.route_address_loading), style = MaterialTheme.typography.bodyLarge)
        }

        is AddressState.Loaded -> Text(address.address, style = MaterialTheme.typography.bodyLarge)

        AddressState.NotFound -> Text(
            text = stringResource(R.string.route_address_not_found),
            style = MaterialTheme.typography.bodyLarge,
        )

        AddressState.Failed -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.route_address_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f, fill = false),
            )
            TextButton(onClick = onRetry) { Text(stringResource(R.string.route_address_retry)) }
        }
    }
}

@Composable
private fun DetailRow(
    @DrawableRes icon: Int,
    content: @Composable () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { content() }
    }
}
