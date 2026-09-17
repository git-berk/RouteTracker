plugins {
    id("routetracker.android.application")
    id("routetracker.android.application.compose")
    id("routetracker.hilt")
}

android {
    namespace = "com.gitberk.routetracker"

    signingConfigs {
        // Committed keystore: the bundled Maps API key only accepts this certificate's SHA-1.
        getByName("debug") {
            storeFile = rootProject.file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.gitberk.routetracker"
        versionCode = 1
        versionName = "1.0"

        manifestPlaceholders["MAPS_API_KEY"] = providers.gradleProperty("MAPS_API_KEY").get()
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.tracking)
    implementation(projects.feature.route)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
