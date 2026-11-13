plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.main.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    implementation(libs.koin.core)
    api(libs.decompose.core)

    api(project(":core:dto"))
    implementation(project(":core:ui"))

    api(project(":features:webview:presentation"))

    api(project(":features:game:domain"))

    api(libs.kotlinx.coroutines.core)

    implementation(libs.androidx.compose.adaptive)

    api(libs.koin.core)

    implementation(libs.coil.compose)

    implementation(libs.essenty.coroutines)
    implementation(libs.androidx.lifecycle.runtime.compose)
}