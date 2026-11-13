plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.pixnpunk.stock.data"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:storage"))
    api(project(":core:database"))
    api(project(":core:dto"))
    implementation(project(":core:notification"))
    implementation(project(":core:logger"))

    api(project(":features:stock:domain"))

    implementation(libs.bundles.roomEcosystem)
    api(libs.bundles.ktorClientEcosystem)

    api(libs.androidx.paging.runtime)
    api(libs.androidx.work)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)

    implementation(project.dependencies.platform(libs.koin.bom))
    api(libs.koin.android)
    implementation(libs.koin.workmanager)

    implementation(libs.storage)

    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.ktor.client.mock)
    testImplementation(libs.ktor.client.mock)
    androidTestImplementation(libs.androidx.concurrent.futures)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.documentfile)
    androidTestImplementation(libs.guava.listenablefuture)
    implementation(libs.kotlinx.serialization.json)
}