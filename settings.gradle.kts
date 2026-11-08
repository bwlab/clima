pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "gree-android"
include(":kotlin-core")
include(":android-app")
project(":kotlin-core").projectDir = file("kotlin-core")
project(":android-app").projectDir = file("android-app")
