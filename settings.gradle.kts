pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        gradlePluginPortal()
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.5.1"
}

stonecutter {
    kotlinController = true
    centralScript = "build.gradle.kts"

    create(rootProject) {
        versions("1.20.4", "1.20.6", "1.21", "1.21.1")
        vcsVersion = "1.20.4"
        branch("rusher")
    }
}

rootProject.name="stonecutter-nuker"