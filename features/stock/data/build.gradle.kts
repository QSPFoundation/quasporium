import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.pixnpunk.stock.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

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
    implementation(project(":core:notification"))

    implementation(project(":features:details:domain"))

    implementation(project(":features:stock:domain"))

    implementation(libs.bundles.roomEcosystem)
    implementation(libs.bundles.ktorClientEcosystem)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.work)

    implementation(libs.kotlinx.coroutines.android)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)

    implementation(libs.jsoup)
    implementation(libs.storage)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
}