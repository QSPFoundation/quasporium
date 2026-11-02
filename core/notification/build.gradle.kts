plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.notification"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.work)

    implementation(platform(libs.koin.bom))
    api(libs.koin.core)
    implementation(libs.androidx.annotation)
}