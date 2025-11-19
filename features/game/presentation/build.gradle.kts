import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.pixnpunk.game.presentation"
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
    implementation(project(":core:utils"))
    implementation(project(":core:database"))
    implementation(project(":core:dto"))
    implementation(project(":core:network"))

    implementation(project(":services:audio"))
    implementation(project(":services:natives"))
    implementation(project(":services:settings"))

    implementation(project(":features:dialogs:presentation"))
    implementation(project(":features:main:presentation"))
    implementation(project(":features:object:presentation"))
    implementation(project(":features:extra:presentation"))

    val composeBom = platform("androidx.compose:compose-bom:2025.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")

    implementation(libs.storage)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.essenty.lifecycle)
    implementation(libs.decompose.core)
    implementation(libs.decompose.ext)
    implementation(libs.decompose.ext.exp)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)
}