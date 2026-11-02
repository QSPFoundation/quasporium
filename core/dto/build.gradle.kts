plugins {
    alias(libs.plugins.quasporium.jvm.library)
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.core)
}