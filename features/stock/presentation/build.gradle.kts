plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.stock.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    implementation(libs.koin.core)
    api(libs.decompose.core)

    implementation(project(":core:ui"))
    api(project(":core:dto"))
    api(project(":core:logger"))

    api(project(":features:stock:domain"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.androidx.paging.runtime)
    api(libs.androidx.paging.compose)

    runtimeOnly(libs.storage)

    api(libs.kotlinx.serialization.json)

    api(libs.koin.core)

    implementation(libs.coil.compose)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    api(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    api(libs.mvikotlin.coro.ext)
    implementation(libs.androidx.window.core)
}