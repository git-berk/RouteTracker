package com.gitberk.routetracker.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Persists whether the user wants tracking on. The service can be killed at any time, so this flag,
 * not the running service, is what decides whether tracking resumes when the app is opened again.
 */
class TrackingPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val isTracking: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[IS_TRACKING] ?: false }
        .distinctUntilChanged()

    suspend fun setTracking(isTracking: Boolean) {
        dataStore.edit { preferences -> preferences[IS_TRACKING] = isTracking }
    }

    private companion object {
        val IS_TRACKING = booleanPreferencesKey("is_tracking")
    }
}
