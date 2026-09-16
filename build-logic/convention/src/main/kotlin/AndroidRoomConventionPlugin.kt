import androidx.room.gradle.RoomExtension
import com.gitberk.routetracker.libs
import com.gitberk.routetracker.library
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("androidx.room")
            pluginManager.apply("com.google.devtools.ksp")

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.library("room-runtime"))
                add("implementation", libs.library("room-ktx"))
                add("ksp", libs.library("room-compiler"))
            }
        }
    }
}
