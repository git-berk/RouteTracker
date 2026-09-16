plugins {
    id("routetracker.android.feature")
}

android {
    namespace = "com.gitberk.routetracker.feature.route"
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(projects.core.data)
    implementation(projects.core.location)
    implementation(projects.core.maps)
    implementation(projects.core.tracking)

    testImplementation(projects.core.testing)
}
