rootProject.name = "Quasporium"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

include(
    ":app",

    ":core",
    ":core:common",
    ":core:database",
    ":core:dto",
    ":core:logger",
    ":core:network",
    ":core:notification",
    ":core:storage",
    ":core:ui",

    ":features",
    ":features:details",
    ":features:details:data",
    ":features:details:domain",
    ":features:details:presentation",
    ":features:dialogs",
    ":features:dialogs:presentation",
    ":features:settings",
    ":features:settings:data",
    ":features:settings:domain",
    ":features:settings:presentation",
    ":features:stock",
    ":features:stock:data",
    ":features:stock:domain",
    ":features:stock:presentation",
    ":features:game",
    ":features:game:data",
    ":features:game:domain",
    ":features:game:presentation",
    ":features:main",
    ":features:main:presentation",
    ":features:extra",
    ":features:extra:presentation",
    ":features:object",
    ":features:object:presentation",
    ":features:webview",
    ":features:webview:data",
    ":features:webview:domain",
    ":features:webview:presentation",
)

plugins {
    id("com.autonomousapps.build-health") version "3.19.2"
    id("com.android.application") version "9.4.1" apply false
    id("com.android.library") version "9.4.1" apply false
    id("org.jetbrains.kotlin.jvm") version "2.4.21" apply false
    id("org.jetbrains.kotlin.android") version "2.4.21" apply false
}