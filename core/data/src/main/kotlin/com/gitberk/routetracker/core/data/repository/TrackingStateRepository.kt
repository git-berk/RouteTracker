package com.gitberk.routetracker.core.data.repository

import com.gitberk.routetracker.core.datastore.TrackingPreferencesDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface TrackingStateRepository {

    val isTracking: Flow<Boolean>

    suspend fun setTracking(isTracking: Boolean)
}

internal class PreferencesTrackingStateRepository @Inject constructor(
    private val dataSource: TrackingPreferencesDataSource,
) : TrackingStateRepository {

    override val isTracking: Flow<Boolean> = dataSource.isTracking

    override suspend fun setTracking(isTracking: Boolean) = dataSource.setTracking(isTracking)
}
