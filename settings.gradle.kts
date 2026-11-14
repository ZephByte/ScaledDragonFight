pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.kikugie.dev/releases") {
            name = "KikuGie"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.9.7"
}

rootProject.name = "ScaledDragonFight"

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"
    create(rootProject) {
        versions("1.21.4", "1.21.5", "1.21.7", "1.21.8", "1.21.9", "1.21.10", "1.21.11", "26.1", "26.2", "26.3")
        vcsVersion = "26.3"
    }
}
