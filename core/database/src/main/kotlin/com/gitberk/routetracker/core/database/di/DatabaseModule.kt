package com.gitberk.routetracker.core.database.di

import android.content.Context
import androidx.room.Room
import com.gitberk.routetracker.core.database.RouteDatabase
import com.gitberk.routetracker.core.database.dao.MarkerDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providesRouteDatabase(@ApplicationContext context: Context): RouteDatabase =
        Room.databaseBuilder(context, RouteDatabase::class.java, "route-database").build()

    @Provides
    fun providesMarkerDao(database: RouteDatabase): MarkerDao = database.markerDao()
}
