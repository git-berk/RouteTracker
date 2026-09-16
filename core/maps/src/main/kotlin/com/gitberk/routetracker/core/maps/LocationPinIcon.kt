package com.gitberk.routetracker.core.maps

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

/**
 * Process-wide cache for the marker icon. Every marker shares one [BitmapDescriptor], so the vector
 * is rasterized once instead of once per marker (or per recomposition).
 *
 * Creation is lazy because [BitmapDescriptorFactory] throws until the Maps SDK is initialized,
 * which only happens once a map is created. Accessed from the main thread only.
 */
object LocationPinIcon {

    /** The pin's tip sits near the bottom of the drawable, so it should touch the coordinate, not its center. */
    val anchor = Offset(0.5f, 22.5f / 24f)

    private var descriptor: BitmapDescriptor? = null

    fun get(context: Context): BitmapDescriptor =
        descriptor ?: create(context).also { descriptor = it }

    private fun create(context: Context): BitmapDescriptor {
        val drawable = requireNotNull(ContextCompat.getDrawable(context, R.drawable.marker_location_pin))
        return BitmapDescriptorFactory.fromBitmap(drawable.toBitmap())
    }
}
