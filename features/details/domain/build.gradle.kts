plugins {
    alias(libs.plugins.quasporium.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:dto"))

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.coroutines.core)
}