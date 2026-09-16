plugins {
    id("routetracker.android.library")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.tracking"
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.location)
    implementation(libs.androidx.core.ktx)
}
