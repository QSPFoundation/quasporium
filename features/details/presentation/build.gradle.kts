plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.details.presentation"
}

dependencies {
    implementation(project(":core:dto"))
    implementation(project(":core:utils"))
    implementation(project(":core:logger"))

    implementation(project(":features:stock:domain"))
    implementation(project(":features:settings:domain"))

    implementation(project(":features:details:data"))
    implementation(project(":features:details:domain"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.ktor3)

    implementation(libs.prettytime)

    implementation(libs.essenty.coroutines)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)

    implementation(libs.storage)
}