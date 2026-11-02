plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.gms.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.detekt) apply false
}

dependencyAnalysis {
    structure {
        ignoreKtx(true)

        bundle("compose") { include("androidx\\.compose\\..*:.*") }
        bundle("lifecycle") { includeGroup("androidx.lifecycle") }
        bundle("paging") { includeGroup("androidx.paging") }
        bundle("room") {
            includeGroup("androidx.room")
            includeGroup("androidx.sqlite")
        }
        bundle("androidx-test") { include("androidx\\.test.*:.*") }
        bundle("decompose") {
            includeGroup("com.arkivanov.decompose")
            includeGroup("com.arkivanov.essenty")
        }
        bundle("mvikotlin") { includeGroup("com.arkivanov.mvikotlin") }
        bundle("koin") { includeGroup("io.insert-koin") }
        bundle("ktor") {
            includeGroup("io.ktor")
            includeGroup("com.squareup.okhttp3")
        }
        bundle("coil") { includeGroup("io.coil-kt.coil3") }
        bundle("coroutines") { include("org\\.jetbrains\\.kotlinx:kotlinx-coroutines-.*") }
        bundle("serialization") { include("org\\.jetbrains\\.kotlinx:kotlinx-serialization-.*") }
    }
    issues {
        all {
            onUnusedDependencies {
                exclude("androidx.compose.ui:ui-tooling-preview")
            }
            onUsedTransitiveDependencies {
                exclude("org.jspecify:jspecify")
            }
        }
    }
}
