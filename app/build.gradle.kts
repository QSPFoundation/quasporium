import com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension
import com.google.firebase.perf.plugin.FirebasePerfExtension

plugins {
    alias(libs.plugins.quasporium.android.application)
    alias(libs.plugins.quasporium.android.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.gms.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
}

android {
    namespace = "com.pixnpunk.quasporium"

    defaultConfig {
        applicationId = "com.pixnpunk.quasporium"
        versionCode = 30140901
        versionName = "1.14.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            ndk { debugSymbolLevel = "FULL" }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
        }
        debug {
            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
                nativeSymbolUploadEnabled = false
            }

            configure<FirebasePerfExtension> {
                setInstrumentationEnabled(false)
            }

            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("foss") {
            dimension = "distribution"
            isDefault = true

            configure<CrashlyticsExtension> {
                mappingFileUploadEnabled = false
                nativeSymbolUploadEnabled = false
            }

            configure<FirebasePerfExtension> {
                setInstrumentationEnabled(false)
            }
        }
        create("play") {
            dimension = "distribution"
        }
    }

    ndkVersion = "27.3.13750724"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:storage"))
    implementation(project(":core:database"))
    implementation(project(":core:dto"))
    implementation(project(":core:network"))
    implementation(project(":core:logger"))
    implementation(project(":core:notification"))

    implementation(project(":features:stock:data"))
    implementation(project(":features:stock:domain"))
    implementation(project(":features:stock:presentation"))

    implementation(project(":features:settings:domain"))
    implementation(project(":features:settings:presentation"))

    implementation(project(":features:dialogs:presentation"))
    implementation(project(":features:details:presentation"))
    implementation(project(":features:game:presentation"))
    implementation(project(":features:webview:presentation"))

    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.appcompat)

    "playImplementation"(platform(libs.firebase.bom))
    "playImplementation"(libs.firebase.crashlytics)
    "playImplementation"(libs.firebase.analytics)
    "playImplementation"(libs.firebase.perf)

    implementation(libs.storage)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)

    implementation(libs.essenty.lifecycle)
    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.core)
    implementation(libs.decompose.ext)
    implementation(libs.decompose.ext.exp)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)

    implementation(libs.preference)
}

tasks.configureEach {
    if (name.startsWith("process") && name.contains("Foss") && name.endsWith("GoogleServices")) {
        enabled = false
    }
}
