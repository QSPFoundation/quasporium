plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.settings.presentation"
}

dependencies {
    implementation(project(":core:dto"))
    implementation(project(":core:utils"))

    implementation(project(":features:settings:data"))
    implementation(project(":features:settings:domain"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)

    implementation(libs.preference)
}
