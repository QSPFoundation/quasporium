plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.quasporium.android.compose)
}

android {
    namespace = "com.pixnpunk.ui"
}

dependencies {
    implementation(project(":core:dto"))

    implementation(libs.androidx.compose.ui)
}
