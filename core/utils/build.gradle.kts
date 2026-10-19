plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.quasporium.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pixnpunk.utils"
}

dependencies {
    implementation(project(":core:dto"))
    implementation(project(":core:logger"))

    implementation(libs.androidx.activity.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.ktor.client.core)

    implementation(libs.storage)

    implementation(libs.jsoup)

    implementation(libs.sevenzip.jbinding.android)
}