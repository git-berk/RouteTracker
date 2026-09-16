plugins {
    id("routetracker.android.library.compose")
}

android {
    namespace = "com.gitberk.routetracker.core.designsystem"
}

dependencies {
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.material3)
}
