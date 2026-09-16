package com.gitberk.routetracker.core.location

import java.io.IOException

interface AddressResolver {

    /** Returns a single-line address, or null when nothing is known at that position. */
    @Throws(IOException::class)
    suspend fun resolve(latitude: Double, longitude: Double): String?
}
