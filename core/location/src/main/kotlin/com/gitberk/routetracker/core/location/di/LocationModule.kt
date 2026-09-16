package com.gitberk.routetracker.core.location.di

import android.content.Context
import com.gitberk.routetracker.core.location.AddressResolver
import com.gitberk.routetracker.core.location.FusedLocationTracker
import com.gitberk.routetracker.core.location.GeocoderAddressResolver
import com.gitberk.routetracker.core.location.LocationTracker
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class LocationModule {

    @Binds
    abstract fun bindsLocationTracker(tracker: FusedLocationTracker): LocationTracker

    @Binds
    abstract fun bindsAddressResolver(resolver: GeocoderAddressResolver): AddressResolver

    companion object {
        @Provides
        fun providesFusedLocationProviderClient(@ApplicationContext context: Context): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}
