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

rootProject.name = "Quasporium"
include(
    ":app",

    ":core",
    ":core:database",
    ":core:dto",
    ":core:logger",
    ":core:network",
    ":core:utils",

    ":features",
    ":features:details",
    ":features:details:presentation",
    ":features:dialogs",
    ":features:dialogs:presentation",
    ":features:settings",
    ":features:settings:data",
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

    ":services",
    ":services:audio",
    ":services:notification"
)
include(":features:settings:domain")
