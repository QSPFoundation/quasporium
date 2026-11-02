plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.extra.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.koin.core)
    api(libs.decompose.core)

    api(project(":core:dto"))

    api(project(":features:webview:presentation"))

    api(project(":features:game:domain"))

    api(libs.kotlinx.coroutines.core)
}