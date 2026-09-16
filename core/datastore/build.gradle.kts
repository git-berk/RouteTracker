plugins {
    id("routetracker.android.library")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(projects.core.common)
}
