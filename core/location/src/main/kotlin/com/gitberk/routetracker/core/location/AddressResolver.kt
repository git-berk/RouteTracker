package com.gitberk.routetracker.core.location

import java.io.IOException

interface AddressResolver {
    @Throws(IOException::class)
    suspend fun resolve(latitude: Double, longitude: Double): String?
}
