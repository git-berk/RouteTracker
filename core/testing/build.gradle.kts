plugins {
    id("routetracker.android.library")
}

android {
    namespace = "com.gitberk.routetracker.core.testing"
}

dependencies {
    api(projects.core.data)
    api(projects.core.location)
    api(projects.core.tracking)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
