package com.gitberk.routetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.gitberk.routetracker.core.designsystem.theme.RouteTrackerTheme
import com.gitberk.routetracker.core.tracking.TrackingController
import com.gitberk.routetracker.feature.route.RouteScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var trackingController: TrackingController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RouteTrackerTheme {
                RouteScreen()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Android only allows starting a location foreground service while the app is visible.
        lifecycleScope.launch { trackingController.resumeIfNeeded() }
    }
}
