package com.gitberk.routetracker.core.data.repository

import com.gitberk.routetracker.core.database.dao.MarkerDao
import com.gitberk.routetracker.core.location.AddressResolver
import com.gitberk.routetracker.core.model.RouteMarker
import java.io.IOException
import javax.inject.Inject

interface AddressRepository {

    /** Returns the marker's address, resolving and caching it on first request. Null when none is known. */
    @Throws(IOException::class)
    suspend fun getAddress(marker: RouteMarker): String?
}

internal class CachingAddressRepository @Inject constructor(
    private val markerDao: MarkerDao,
    private val addressResolver: AddressResolver,
) : AddressRepository {

    override suspend fun getAddress(marker: RouteMarker): String? {
        marker.address?.let { return it }
        return addressResolver.resolve(marker.latitude, marker.longitude)
            ?.also { address -> markerDao.updateAddress(marker.id, address) }
    }
}
