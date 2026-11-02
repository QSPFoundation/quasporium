plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.ui.webview"
}

dependencies {
    api(libs.androidx.compose.material3)
    api(libs.koin.core)
    api(libs.decompose.core)

    implementation(project(":core:common"))
    implementation(project(":core:storage"))
    api(project(":core:dto"))

    api(project(":features:webview:domain"))

    api(project(":features:game:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.webkit)

    implementation(libs.storage)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    implementation(libs.androidx.documentfile)
    api(libs.kotlinx.serialization.core)
}