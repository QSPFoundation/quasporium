plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pixnpunk.network"
}

dependencies {
    implementation(libs.bundles.ktorClientEcosystem)

    api(libs.coil.core)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.ktor3)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)

    testImplementation(libs.ktor.client.mock)
    androidTestImplementation(libs.ktor.client.mock)
}