import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("quasporium-android-library"))
        pluginManager.apply(libs.pluginId("quasporium-android-compose"))
        pluginManager.apply(libs.pluginId("kotlin-serialization"))

        dependencies {
            "implementation"(libs.library("androidx-compose-material3"))
            "implementation"(libs.library("androidx-compose-material-icons-extended"))

            "implementation"(platform(libs.library("koin-bom")))
            "implementation"(libs.library("koin-android"))

            "implementation"(libs.library("essenty-lifecycle"))
            "implementation"(libs.library("decompose-core"))
            "implementation"(libs.library("decompose-ext"))
        }
    }
}
