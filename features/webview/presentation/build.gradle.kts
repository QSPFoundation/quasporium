plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.ui.webview"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:storage"))
    implementation(project(":core:ui"))
    implementation(project(":core:dto"))

    implementation(project(":features:webview:domain"))

    implementation(project(":features:game:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.webkit)

    implementation(libs.storage)
    implementation(libs.jsoup)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)
}