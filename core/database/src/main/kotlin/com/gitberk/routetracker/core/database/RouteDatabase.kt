package com.gitberk.routetracker.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gitberk.routetracker.core.database.dao.MarkerDao
import com.gitberk.routetracker.core.database.model.MarkerEntity

@Database(
    entities = [MarkerEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao
}
