plugins {
    id("routetracker.android.library")
    id("routetracker.android.room")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker.core.database"
}

dependencies {
    api(projects.core.model)
}
