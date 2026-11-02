plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.storage"
}

dependencies {
    implementation(project(":core:common"))
    api(project(":core:dto"))
    implementation(project(":core:logger"))

    implementation(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.core)

    implementation(platform(libs.koin.bom))
    api(libs.koin.android)

    api(libs.ktor.client.core)

    implementation(libs.storage)

    implementation(libs.sevenzip.jbinding.android)
    api(libs.androidx.documentfile)
}
