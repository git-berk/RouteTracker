import java.util.Properties

plugins {
    id("routetracker.android.application")
    id("routetracker.android.application.compose")
    id("routetracker.hilt")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "com.gitberk.routetracker"

    signingConfigs {
        // Shared debug keystore so every clone builds with the same SHA-1, which the bundled
        // Maps API key is restricted to. It signs debug builds only and protects nothing.
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

        // local.properties and the environment take precedence so anyone can use their own key
        // instead of the restricted one bundled in gradle.properties.
        manifestPlaceholders["MAPS_API_KEY"] = localProperties.getProperty("MAPS_API_KEY")
            ?: System.getenv("MAPS_API_KEY")
            ?: providers.gradleProperty("MAPS_API_KEY").orNull.orEmpty()
    }
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(projects.core.tracking)
    implementation(projects.feature.route)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
}
