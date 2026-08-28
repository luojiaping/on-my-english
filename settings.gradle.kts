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
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "on-my-english"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")
include(":core:ai")
include(":core:common")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:designsystem")
include(":core:domain")
include(":core:model")
include(":core:network")
include(":core:ui")
include(":feature:settings")
include(":feature:stats")
include(":feature:study")
include(":feature:wordbook")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    On My English requires JDK 17 or newer.
    Current JDK: ${JavaVersion.current()}
    Java home: ${System.getProperty("java.home")}
    """.trimIndent()
}
