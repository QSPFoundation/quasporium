plugins {
    alias(libs.plugins.quasporium.jvm.library)
}

dependencies {
    api(project(":core:dto"))

    api(libs.kotlinx.coroutines.core)
}
