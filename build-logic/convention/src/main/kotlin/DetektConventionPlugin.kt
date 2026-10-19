import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("detekt"))

        extensions.configure<DetektExtension> {
            parallel = true
            config.setFrom(isolated.rootProject.projectDirectory.file("config/detekt/detekt.yml"))
            buildUponDefaultConfig = true
        }

        dependencies {
            "detektPlugins"(libs.library("detekt-formatting"))
            "detektPlugins"(libs.library("detekt-rules-compose"))
        }
    }
}
