pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
    }
}
rootProject.name = "tengu"

include(":framework")
include(":common:module-visitor")
include(":common:repository")
include(":app-hosting")
include(":app")
include(":desktop-app")
include(":feature:acp")
