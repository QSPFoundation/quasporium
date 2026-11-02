plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.settings.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.koin.core)
    api(libs.decompose.core)

    api(project(":core:dto"))
    implementation(project(":core:logger"))

    api(project(":features:settings:domain"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)

    api(libs.preference)
    implementation(libs.androidx.window.core)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
}
