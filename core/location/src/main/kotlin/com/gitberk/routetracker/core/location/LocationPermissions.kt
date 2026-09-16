package com.gitberk.routetracker.core.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

// Only precise location is accepted: approximate fixes are kilometers off and would never
// pass the accuracy filter, so tracking would silently record nothing.
fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
