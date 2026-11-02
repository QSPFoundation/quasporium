plugins {
    alias(libs.plugins.quasporium.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:dto"))

    api(libs.kotlinx.serialization.core)
    api(libs.kotlinx.coroutines.core)
}