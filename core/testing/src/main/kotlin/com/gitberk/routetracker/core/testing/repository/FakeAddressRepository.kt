package com.gitberk.routetracker.core.testing.repository

import com.gitberk.routetracker.core.data.repository.AddressRepository
import com.gitberk.routetracker.core.model.RouteMarker
import java.io.IOException

class FakeAddressRepository : AddressRepository {

    var address: String? = null
    var failure: IOException? = null
    var requestCount = 0
        private set

    override suspend fun getAddress(marker: RouteMarker): String? {
        requestCount++
        failure?.let { throw it }
        return address
    }
}
