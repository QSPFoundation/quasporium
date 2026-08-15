rootProject.name = "Quasporium"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
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
    ":core:database",
    ":core:dto",
    ":core:logger",
    ":core:network",
    ":core:utils",
    ":core:notification",

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
