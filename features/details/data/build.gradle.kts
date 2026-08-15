import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.pixnpunk.details.data"
    compileSdk = 37

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

    implementation(project(":features:details:domain"))

    implementation(libs.bundles.roomEcosystem)
    implementation(libs.bundles.ktorClientEcosystem)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.work)

    implementation(libs.kotlinx.coroutines.android)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.jsoup)
    implementation(libs.storage)
}