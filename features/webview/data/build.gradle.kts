plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.webview.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:dto"))

    implementation(project(":features:webview:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.android)

    implementation(libs.jsoup)
}