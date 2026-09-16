package com.gitberk.routetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gitberk.routetracker.core.designsystem.theme.RouteTrackerTheme
import com.gitberk.routetracker.feature.route.RouteScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RouteTrackerTheme {
                RouteScreen()
            }
        }
    }
}
