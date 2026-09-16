package com.gitberk.routetracker.core.designsystem.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.gitberk.routetracker.core.designsystem.icon.RouteTrackerIcons
import com.gitberk.routetracker.core.designsystem.theme.RouteTrackerTheme

@Composable
fun MapActionButton(
    text: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = { Icon(painter = painterResource(icon), contentDescription = null) },
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
    )
}

@Composable
fun MapIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SmallFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        elevation = FloatingActionButtonDefaults.elevation(),
    ) {
        Icon(painter = painterResource(icon), contentDescription = contentDescription)
    }
}

@Preview
@Composable
private fun MapActionButtonPreview() {
    RouteTrackerTheme {
        MapActionButton(text = "Start", icon = RouteTrackerIcons.Play, onClick = {})
    }
}
