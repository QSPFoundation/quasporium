plugins {
    alias(libs.plugins.quasporium.android.library)
}

android {
    namespace = "com.pixnpunk.logger"
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    implementation(project.dependencies.platform(libs.koin.bom))
    runtimeOnly(libs.kotlinx.coroutines.android)
}