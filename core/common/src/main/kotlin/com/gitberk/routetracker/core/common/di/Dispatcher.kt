package com.gitberk.routetracker.core.common.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: RouteDispatchers)

enum class RouteDispatchers {
    Default,
    IO,
}
