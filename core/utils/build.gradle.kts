import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.pixnpunk.utils"
    compileSdk = 36

    defaultConfig { minSdk = 26 }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
}

dependencies {
    implementation(project(":core:dto"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.ktor.client.core)

    implementation(libs.storage)

    implementation("org.jsoup:jsoup:1.21.2")

    implementation("com.github.omicronapps:7-Zip-JBinding-4Android:Release-16.02-2.03")
}