plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.game.presentation"
}

dependencies {
    implementation(project(":core:storage"))
    implementation(project(":core:dto"))
    implementation(project(":core:network"))

    implementation(project(":features:game:data"))
    implementation(project(":features:game:domain"))

    implementation(project(":features:settings:domain"))

    implementation(project(":features:dialogs:presentation"))
    implementation(project(":features:main:presentation"))
    implementation(project(":features:object:presentation"))
    implementation(project(":features:extra:presentation"))

    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.adaptive.layout)
    implementation(libs.androidx.compose.adaptive.navigation)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.storage)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)
}