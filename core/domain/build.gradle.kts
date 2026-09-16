plugins {
    id("routetracker.android.library")
}

android {
    namespace = "com.gitberk.routetracker.core.domain"
}

dependencies {
    api(projects.core.data)
    implementation(projects.core.common)
    implementation(libs.hilt.android)
}
