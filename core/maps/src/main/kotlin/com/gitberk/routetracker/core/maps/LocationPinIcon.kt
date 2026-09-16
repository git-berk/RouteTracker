package com.gitberk.routetracker.core.maps

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

// Created lazily: BitmapDescriptorFactory throws until a map has initialized the Maps SDK.
object LocationPinIcon {
    val anchor = Offset(0.5f, 22.5f / 24f)

    private var descriptor: BitmapDescriptor? = null

    fun get(context: Context): BitmapDescriptor =
        descriptor ?: create(context).also { descriptor = it }

    private fun create(context: Context): BitmapDescriptor {
        val drawable = requireNotNull(ContextCompat.getDrawable(context, R.drawable.marker_location_pin))
        return BitmapDescriptorFactory.fromBitmap(drawable.toBitmap())
    }
}
