plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.game.presentation"
}

dependencies {
    implementation(libs.androidx.compose.material3)
    implementation(libs.koin.core)
    api(libs.decompose.core)

    implementation(project(":core:storage"))
    api(project(":core:dto"))
    implementation(project(":core:ui"))

    api(project(":features:game:domain"))

    implementation(project(":features:settings:domain"))

    api(project(":features:dialogs:presentation"))
    api(project(":features:main:presentation"))
    api(project(":features:object:presentation"))
    api(project(":features:extra:presentation"))

    implementation(libs.androidx.compose.adaptive)
    api(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    api(libs.androidx.compose.material3.adaptive.navigation.suite)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.storage)

    api(libs.koin.android)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    api(libs.androidx.activity)
    api(libs.kotlinx.serialization.core)
}