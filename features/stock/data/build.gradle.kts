plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.stock.data"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:database"))
    implementation(project(":core:dto"))
    implementation(project(":core:network"))
    implementation(project(":core:notification"))
    implementation(project(":core:logger"))

    implementation(project(":features:details:domain"))

    implementation(project(":features:stock:domain"))

    implementation(libs.bundles.roomEcosystem)
    implementation(libs.bundles.ktorClientEcosystem)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.work)

    implementation(libs.kotlinx.coroutines.android)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.workmanager)

    implementation(libs.jsoup)
    implementation(libs.storage)
}