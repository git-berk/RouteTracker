package com.gitberk.routetracker.core.tracking

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import com.gitberk.routetracker.core.domain.RecordLocationUseCase
import com.gitberk.routetracker.core.location.LocationTracker
import com.gitberk.routetracker.core.location.hasLocationPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "LocationTrackingService"
private const val NOTIFICATION_ID = 1
private const val ACTION_STOP = "com.gitberk.routetracker.action.STOP_TRACKING"

/**
 * Keeps receiving location updates while the app is in the background or removed from recents.
 * It only records markers; the UI observes them from the database, so the two never talk directly.
 */
@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject internal lateinit var locationTracker: LocationTracker

    @Inject internal lateinit var recordLocation: RecordLocationUseCase

    @Inject internal lateinit var routeRepository: RouteRepository

    @Inject internal lateinit var trackingStateRepository: TrackingStateRepository

    @Inject internal lateinit var notifier: TrackingNotifier

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var trackingJob: Job? = null
    private var markerCount = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopFromNotification()
            return START_NOT_STICKY
        }
        if (!hasLocationPermission() || !startInForeground()) {
            stopSelf()
            return START_NOT_STICKY
        }

        // Start requests can repeat (app reopened, sticky restart); only one collection should run.
        if (trackingJob == null) {
            trackingJob = serviceScope.launch {
                launch { observeMarkerCount() }
                collectLocations()
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private suspend fun collectLocations() {
        locationTracker.locationUpdates()
            .catch { error ->
                Log.e(TAG, "Location updates failed", error)
                trackingStateRepository.setTracking(false)
                stopSelf()
            }
            .collect { point -> recordLocation(point) }
    }

    private suspend fun observeMarkerCount() {
        routeRepository.markers
            .map { markers -> markers.size }
            .distinctUntilChanged()
            .collect { count ->
                markerCount = count
                notifier.update(NOTIFICATION_ID, count)
            }
    }

    private fun stopFromNotification() {
        serviceScope.launch {
            trackingStateRepository.setTracking(false)
            stopSelf()
        }
    }

    /**
     * A sticky restart can happen while the app is in the background, where Android refuses
     * location foreground services. Tracking stays flagged on so it resumes when the app is opened.
     */
    private fun startInForeground(): Boolean = try {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notifier.createNotification(markerCount),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION else 0,
        )
        true
    } catch (e: RuntimeException) {
        Log.w(TAG, "Could not start in foreground", e)
        false
    }

    companion object {
        fun intent(context: Context) = Intent(context, LocationTrackingService::class.java)

        internal fun stopIntent(context: Context) = intent(context).setAction(ACTION_STOP)
    }
}
