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
    }
}

rootProject.name = "ToolBox"

include(":app")

// Core modules
include(":core:core-common")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-security")
include(":core:core-network")
include(":core:core-ai")
include(":core:core-notification")
include(":core:core-alarm")
include(":core:core-websnapshot")
include(":core:core-ui")

// Feature modules
include(":feature:feature-memo")
include(":feature:feature-schedule")
include(":feature:feature-checkin")
include(":feature:feature-password")
include(":feature:feature-inspiration")
