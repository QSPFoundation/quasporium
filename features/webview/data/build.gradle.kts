plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.webview.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:dto"))

    api(project(":features:webview:domain"))

    implementation(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    api(libs.koin.core)

    implementation(libs.jsoup)
    runtimeOnly(libs.kotlinx.coroutines.android)
}