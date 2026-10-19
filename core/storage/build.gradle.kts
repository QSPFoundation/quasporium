plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.storage"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:dto"))
    implementation(project(":core:logger"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)

    implementation(libs.ktor.client.core)

    implementation(libs.storage)

    implementation(libs.sevenzip.jbinding.android)
}
