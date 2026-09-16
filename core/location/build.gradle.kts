plugins {
    id("routetracker.android.library")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.location"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.common)
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.location)
    implementation(libs.kotlinx.coroutines.play.services)
}
