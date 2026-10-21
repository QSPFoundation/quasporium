import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.CommonExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

internal val JAVA_VERSION = JavaVersion.VERSION_17
internal val JVM_TARGET = JvmTarget.JVM_17

internal fun Project.configureKotlinAndroid(
    extension: ApplicationExtension
) = with(extension) {
    compileSdk = libs.versionInt("compileSdk")

    defaultConfig {
        minSdk = libs.versionInt("minSdk")
        targetSdk = libs.versionInt("targetSdk")
    }

    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    configureKotlinJvmTarget()
}

internal fun Project.configureKotlinAndroid(
    extension: LibraryExtension
) = with(extension) {
    compileSdk = libs.versionInt("compileSdk")

    defaultConfig {
        minSdk = libs.versionInt("minSdk")
    }

    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    configureKotlinJvmTarget()
}

internal fun Project.configureKotlinJvmTarget() =
    tasks.withType<KotlinJvmCompile>().configureEach {
        compilerOptions.jvmTarget.set(JVM_TARGET)
    }

internal fun Project.configureUnitTests() = dependencies {
    "testImplementation"(libs.library("junit"))
    "testImplementation"(libs.library("kotlinx-coroutines-test"))
}

internal fun Project.configureInstrumentedTests(extension: CommonExtension) = with(extension) {
    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    testOptions.managedDevices.allDevices.create<ManagedVirtualDevice>("atd") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp-atd"
    }

    dependencies {
        "androidTestImplementation"(libs.library("androidx-junit"))
        "androidTestImplementation"(libs.library("androidx-test-core"))
        "androidTestImplementation"(libs.library("androidx-test-rules"))
        "androidTestImplementation"(libs.library("androidx-test-runner"))
        "androidTestImplementation"(libs.library("androidx-espresso-core"))
        "androidTestImplementation"(libs.library("kotlinx-coroutines-test"))
    }
}
