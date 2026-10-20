plugins {
    alias(libs.plugins.quasporium.jvm.library)
}

dependencies {
    implementation(project(":core:dto"))

    implementation(libs.kotlinx.coroutines.core)
}
