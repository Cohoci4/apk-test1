pluginManagement {
    repositories {
        google()
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

rootProject.name = "AIFoodEnhancer"

include(":app")
include(":core")
include(":core-ml")
include(":core-network")
include(":domain")
include(":data")
include(":feature-camera")
include(":feature-editor")
