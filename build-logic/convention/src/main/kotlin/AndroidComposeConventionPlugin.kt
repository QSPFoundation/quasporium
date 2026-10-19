import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("compose-compiler"))

        pluginManager.withPlugin(libs.pluginId("android-application")) {
            extensions.configure<ApplicationExtension> { buildFeatures.compose = true }
        }
        pluginManager.withPlugin(libs.pluginId("android-library")) {
            extensions.configure<LibraryExtension> { buildFeatures.compose = true }
        }

        dependencies {
            val bom = platform(libs.library("androidx-compose-bom"))
            "implementation"(bom)
            "androidTestImplementation"(bom)
            "implementation"(libs.library("androidx-compose-ui-tooling-preview"))
            "debugImplementation"(libs.library("androidx-compose-ui-tooling"))
        }
    }
}
