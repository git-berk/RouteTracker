plugins {
    id("routetracker.android.library")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.common"
}

dependencies {
    implementation(libs.kotlinx.coroutines.android)
}
