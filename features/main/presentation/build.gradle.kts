plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.main.presentation"
}

dependencies {
    implementation(project(":core:dto"))
    implementation(project(":core:utils"))

    implementation(project(":features:webview:presentation"))

    implementation(project(":features:game:data"))
    implementation(project(":features:game:domain"))

    implementation(project(":features:settings:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.koin.core)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    implementation(libs.essenty.coroutines)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)
}