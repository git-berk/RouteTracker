package com.gitberk.routetracker.core.location

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.gitberk.routetracker.core.model.LocationPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val UPDATE_INTERVAL_MILLIS = 5_000L

// Well below the 100 m marker distance so a marker is never placed late, while still letting
// the provider skip deliveries when the user stands still.
private const val MIN_UPDATE_DISTANCE_METERS = 20f

// Callers are expected to check permission first (see hasLocationPermission); a missing
// permission surfaces as a SecurityException through the flow or the suspend call.
@SuppressLint("MissingPermission")
internal class FusedLocationTracker @Inject constructor(
    private val client: FusedLocationProviderClient,
) : LocationTracker {

    override fun locationUpdates(): Flow<LocationPoint> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MILLIS)
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location -> trySend(location.asLocationPoint()) }
            }
        }
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            .addOnFailureListener { error -> close(error) }
        awaitClose { client.removeLocationUpdates(callback) }
    }

    override suspend fun currentLocation(): LocationPoint? =
        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()?.asLocationPoint()
}

private fun Location.asLocationPoint() = LocationPoint(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    timestampMillis = time,
)
