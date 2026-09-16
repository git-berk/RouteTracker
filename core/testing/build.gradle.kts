plugins {
    id("routetracker.android.library")
}

android {
    namespace = "com.gitberk.routetracker.core.testing"
}

dependencies {
    api(projects.core.data)
    api(projects.core.location)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
}
