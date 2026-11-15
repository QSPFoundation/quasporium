import com.android.build.api.dsl.ManagedVirtualDevice

plugins {
    id("com.android.test")
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.quasporium.detekt)
}

android {
    namespace = "com.pixnpunk.quasporium.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    targetProjectPath = ":app"

    flavorDimensions += "distribution"
    productFlavors {
        create("foss") { dimension = "distribution" }
        create("play") { dimension = "distribution" }
    }

    testOptions.managedDevices.allDevices.create<ManagedVirtualDevice>("pixel6Api34") {
        device = "Pixel 6"
        apiLevel = 34
        systemImageSource = "aosp"
    }
}

baselineProfile {
    managedDevices += "pixel6Api34"
    useConnectedDevices = false
}

androidComponents {
    beforeVariants(selector().withFlavor("distribution" to "play")) { it.enable = false }
}

dependencies {
    implementation(libs.junit)
    implementation(libs.androidx.junit)
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.benchmark.macro.junit4)
    implementation(libs.androidx.uiautomator)
}
