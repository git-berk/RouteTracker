plugins {
    id("routetracker.android.library")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.data"
}

dependencies {
    api(projects.core.model)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.location)
}
