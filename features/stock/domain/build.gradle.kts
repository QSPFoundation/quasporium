plugins {
    alias(libs.plugins.quasporium.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:dto"))

    implementation(libs.androidx.paging.common)

    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
}