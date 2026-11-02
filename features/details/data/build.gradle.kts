plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.details.data"
}

dependencies {
    api(project(":core:database"))

    api(project(":features:details:domain"))

    implementation(libs.bundles.roomEcosystem)

    implementation(libs.androidx.work)

    api(libs.kotlinx.coroutines.core)

    implementation(project.dependencies.platform(libs.koin.bom))
    api(libs.koin.core)

    implementation(libs.storage)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
}