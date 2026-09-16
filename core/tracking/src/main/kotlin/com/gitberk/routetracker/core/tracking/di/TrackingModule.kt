package com.gitberk.routetracker.core.tracking.di

import com.gitberk.routetracker.core.tracking.ServiceTrackingController
import com.gitberk.routetracker.core.tracking.TrackingController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TrackingModule {

    @Binds
    abstract fun bindsTrackingController(controller: ServiceTrackingController): TrackingController
}
