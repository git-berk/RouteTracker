plugins {
    id("routetracker.android.feature")
}

android {
    namespace = "com.gitberk.routetracker.feature.route"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.maps)
}
