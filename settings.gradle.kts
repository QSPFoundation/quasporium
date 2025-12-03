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
    ":core:network",
    ":core:utils",

    ":features",
    ":features:dialogs",
    ":features:dialogs:presentation",
    ":features:details",
    ":features:details:presentation",
    ":features:settings",
    ":features:settings:presentation",
    ":features:stock",
    ":features:stock:data",
    ":features:stock:domain",
    ":features:stock:presentation",
    ":features:game",
    ":features:game:presentation",
    ":features:main:presentation",
    ":features:extra:presentation",
    ":features:object:presentation",
    ":features:dialogs:presentation",

    ":services",
    ":services:audio",
    ":services:natives",
    ":services:settings"
)
