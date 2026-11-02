plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.dialogs.presentation"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.decompose.core)

    api(project(":core:dto"))
    implementation(project(":core:storage"))

    api(project(":features:game:domain"))

    implementation(libs.coil.compose)
    runtimeOnly(libs.coil.gif)
    implementation(libs.coil.network.ktor3)

    implementation(libs.colorpicker.compose)

    implementation(libs.prettytime)

    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.serialization.core)
}