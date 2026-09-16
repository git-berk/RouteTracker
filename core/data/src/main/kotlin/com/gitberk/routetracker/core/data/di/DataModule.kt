package com.gitberk.routetracker.core.data.di

import com.gitberk.routetracker.core.data.repository.AddressRepository
import com.gitberk.routetracker.core.data.repository.CachingAddressRepository
import com.gitberk.routetracker.core.data.repository.OfflineRouteRepository
import com.gitberk.routetracker.core.data.repository.PreferencesTrackingStateRepository
import com.gitberk.routetracker.core.data.repository.RouteRepository
import com.gitberk.routetracker.core.data.repository.TrackingStateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    abstract fun bindsRouteRepository(repository: OfflineRouteRepository): RouteRepository

    @Binds
    abstract fun bindsTrackingStateRepository(repository: PreferencesTrackingStateRepository): TrackingStateRepository

    @Binds
    abstract fun bindsAddressRepository(repository: CachingAddressRepository): AddressRepository
}
