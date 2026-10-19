plugins {
    alias(libs.plugins.quasporium.android.feature)
}

android {
    namespace = "com.pixnpunk.stock.presentation"
}

dependencies {
    implementation(project(":core:utils"))
    implementation(project(":core:database"))
    implementation(project(":core:dto"))
    implementation(project(":core:logger"))

    implementation(project(":features:stock:data"))
    implementation(project(":features:stock:domain"))

    implementation(project(":features:details:presentation"))

    implementation(project(":features:settings:data"))

    implementation(libs.androidx.compose.adaptive)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    implementation(libs.storage)

    implementation(libs.coil.gif)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.koin.core)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.ktor3)
    
    implementation(libs.essenty.coroutines)
    implementation(libs.decompose.ext.exp)
    implementation(libs.mvikotlin.core)
    implementation(libs.mvikotlin.main)
    implementation(libs.mvikotlin.coro.ext)
}