import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
}

detekt {
    parallel = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

android {
    namespace = "com.pixnpunk.game.data"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            debugSymbolLevel = "FULL"
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DBUILD_JAVA=1",
                    "-DBUILD_JVM=1",
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=1"
                )
            }
        }
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

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    ndkVersion = "27.3.13750724"
    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":core:dto"))
    implementation(project(":core:utils"))
    implementation(project(":core:logger"))
    implementation(project(":core:database"))

    implementation(project(":features:game:domain"))

    implementation(project(":features:settings:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.kotlinx.coroutines.android)
    implementation("org.jetbrains.kotlinx:atomicfu:0.33.0")

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.storage)

    implementation(libs.decompose.core)
}