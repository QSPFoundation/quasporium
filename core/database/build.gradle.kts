plugins {
    alias(libs.plugins.quasporium.android.library)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
}

room {
    schemaDirectory("$projectDir/schemas")
}

android {
    namespace = "com.pixnpunk.database"
}

dependencies {
    api(project(":core:dto"))

    api(libs.bundles.roomEcosystem)

    api(libs.androidx.paging.runtime)

    api(libs.kotlinx.coroutines.core)

    implementation(project.dependencies.platform(libs.koin.bom))
    api(libs.koin.android)

    ksp(libs.androidx.room.compiler)

    androidTestImplementation(libs.androidx.room.testing)
}