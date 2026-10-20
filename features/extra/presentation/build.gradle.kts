plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.extra.presentation"
}

dependencies {
    implementation(project(":core:dto"))

    implementation(project(":features:webview:presentation"))

    implementation(project(":features:game:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.kotlinx.coroutines.android)
}