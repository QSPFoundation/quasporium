plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pixnpunk.network"
}

dependencies {
    implementation(libs.bundles.ktorClientEcosystem)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)
}