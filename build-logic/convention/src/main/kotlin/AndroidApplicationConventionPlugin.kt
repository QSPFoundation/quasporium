import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply(libs.pluginId("android-application"))
        pluginManager.apply(libs.pluginId("quasporium-detekt"))

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            configureInstrumentedTests(this)
        }

        configureUnitTests()
    }
}
