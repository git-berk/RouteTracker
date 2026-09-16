package com.gitberk.routetracker.core.data.repository

import com.gitberk.routetracker.core.database.dao.MarkerDao
import com.gitberk.routetracker.core.database.model.MarkerEntity
import com.gitberk.routetracker.core.database.model.asExternalModel
import com.gitberk.routetracker.core.model.LocationPoint
import com.gitberk.routetracker.core.model.RouteMarker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class OfflineRouteRepository @Inject constructor(
    private val markerDao: MarkerDao,
) : RouteRepository {

    override val markers: Flow<List<RouteMarker>> =
        markerDao.observeMarkers().map { entities -> entities.map(MarkerEntity::asExternalModel) }

    override suspend fun getLastMarker(): RouteMarker? = markerDao.getLastMarker()?.asExternalModel()

    override suspend fun addMarker(point: LocationPoint) {
        markerDao.insert(
            MarkerEntity(
                latitude = point.latitude,
                longitude = point.longitude,
                recordedAtMillis = point.timestampMillis,
            ),
        )
    }

    override suspend fun clearRoute() = markerDao.deleteAll()
}
