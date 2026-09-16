package com.gitberk.routetracker.core.tracking

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val CHANNEL_ID = "route_tracking"

internal class TrackingNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun createNotification(markerCount: Int): Notification {
        ensureChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_tracking)
            .setContentTitle(context.getString(R.string.tracking_notification_title))
            .setContentText(
                context.resources.getQuantityString(R.plurals.tracking_notification_marker_count, markerCount, markerCount),
            )
            .setContentIntent(openAppIntent())
            .addAction(0, context.getString(R.string.tracking_notification_stop), stopTrackingIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    fun update(notificationId: Int, markerCount: Int) {
        // Without the permission the foreground notification is hidden anyway; tracking keeps working.
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(context).notify(notificationId, createNotification(markerCount))
    }

    private fun openAppIntent(): PendingIntent? {
        // This module can't reference MainActivity, so it opens whatever the launcher would open.
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return null
        return PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun stopTrackingIntent(): PendingIntent = PendingIntent.getService(
        context,
        0,
        LocationTrackingService.stopIntent(context),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    private fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.tracking_channel_name),
            // Low importance keeps the ongoing notification silent.
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.tracking_channel_description)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
