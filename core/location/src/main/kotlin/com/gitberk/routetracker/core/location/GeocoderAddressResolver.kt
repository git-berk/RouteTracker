package com.gitberk.routetracker.core.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.gitberk.routetracker.core.common.di.Dispatcher
import com.gitberk.routetracker.core.common.di.RouteDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class GeocoderAddressResolver @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(RouteDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AddressResolver {

    override suspend fun resolve(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) throw IOException("Geocoder is not available on this device")

        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.awaitAddresses(latitude, longitude)
        } else {
            withContext(ioDispatcher) {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(latitude, longitude, 1).orEmpty()
            }
        }
        return addresses.firstOrNull()?.singleLine()
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun Geocoder.awaitAddresses(latitude: Double, longitude: Double): List<Address> =
    suspendCancellableCoroutine { continuation ->
        getFromLocation(
            latitude,
            longitude,
            1,
            object : Geocoder.GeocodeListener {
                override fun onGeocode(addresses: MutableList<Address>) {
                    continuation.resume(addresses)
                }

                override fun onError(errorMessage: String?) {
                    continuation.resumeWithException(IOException(errorMessage))
                }
            },
        )
    }

private fun Address.singleLine(): String? =
    if (maxAddressLineIndex >= 0) getAddressLine(0) else null
