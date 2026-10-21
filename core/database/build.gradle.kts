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
    implementation(project(":core:dto"))

    implementation(libs.bundles.roomEcosystem)

    implementation(libs.androidx.paging.runtime)

    implementation(libs.kotlinx.coroutines.android)

    implementation(project.dependencies.platform(libs.koin.bom))
    implementation(libs.koin.android)

    ksp(libs.androidx.room.compiler)

    androidTestImplementation(libs.androidx.room.testing)
}