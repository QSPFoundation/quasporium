plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.details.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.koin.core)
    api(libs.decompose.core)

    api(project(":core:dto"))
    api(project(":core:storage"))
    implementation(project(":core:ui"))
    api(project(":core:logger"))

    api(project(":features:stock:domain"))
    api(project(":features:settings:domain"))

    api(project(":features:details:domain"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.coil.compose)

    implementation(libs.prettytime)

    implementation(libs.essenty.coroutines)
    api(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    api(libs.mvikotlin.coro.ext)

    runtimeOnly(libs.storage)
    implementation(libs.androidx.window.core)
    api(libs.kotlinx.serialization.core)
}