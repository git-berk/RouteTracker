plugins {
    id("routetracker.android.library.compose")
}

android {
    namespace = "com.gitberk.routetracker.core.maps"
}

dependencies {
    api(projects.core.model)
    api(libs.maps.compose)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.core.ktx)
}
