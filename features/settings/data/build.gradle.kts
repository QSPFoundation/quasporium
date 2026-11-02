plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.settings.data"
}

dependencies {
    api(project(":core:dto"))

    api(project(":features:settings:domain"))

    implementation(libs.androidx.core.ktx)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)
}
