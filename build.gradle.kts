import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.9.1"
    id("maven-publish")
}
val modId = project.property("mod_id") as String
val modName = project.property("mod_name") as String
val commandPrefix = project.property("command_prefix") as String
val rusherWrapperVersion = project.property("rusher_wrapper_version")
version = project.property("mod_version") as String
group = project.property("maven_group") as String

base.archivesName = modId + "-" + stonecutter.current.project

val targetJavaVersion = if (stonecutter.eval(stonecutter.current.version, "<1.20.5")) 17 else 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    withSourcesJar()
}

loom {
    runConfigs.all {
        ideConfigGenerated(true) // Run configurations are not created for subprojects by default
        runDir = "../../run" // Use a shared run folder and create separate worlds
    }
}

allprojects {
    repositories {
        exclusiveContent {
            forRepository { maven("https://api.modrinth.com/maven") }
            filter { includeGroup("maven.modrinth") }
        }
        maven("https://maven.meteordev.org/releases") // Meteor
        maven("https://maven.meteordev.org/snapshots") // Baritone/ meteor
        maven("https://babbaj.github.io/maven/") //Nether Pathfinder
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")
    include("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")
    include("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    modImplementation("meteordevelopment:baritone:${stonecutter.current.project}-SNAPSHOT")
    modImplementation("meteordevelopment:meteor-client:${project.property("meteor_version")}")

    modImplementation("maven.modrinth:malilib:${project.property("malilib_version")}")
    modImplementation("maven.modrinth:litematica:${project.property("litematica_version")}")
    modRuntimeOnly("dev.babbaj:nether-pathfinder:${project.property("nether_pathfinder_version")}")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        inputs.property("minecraft_version", stonecutter.current.project)
        inputs.property("loader_version", project.property("loader_version"))
        inputs.property("kotlin_loader_version", project.property("kotlin_loader_version"))
        inputs.property("mod_id", modId)
        inputs.property("mod_name", modName)
        inputs.property("command_prefix", commandPrefix)
        inputs.property("mod_version", project.version)
        inputs.property("rusher_wrapper_version", rusherWrapperVersion)

        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand(
                "mod_id" to modId,
                "mod_version" to project.version,
                "mod_name" to modName,
                "minecraft_version" to stonecutter.current.project,
                "loader_version" to project.property("loader_version"),
                "kotlin_loader_version" to project.property("kotlin_loader_version"),
            )
        }

        filesMatching("merge.json") {
            expand(
                "mod_id" to modId,
                "mod_name" to modName,
                "command_prefix" to commandPrefix,
                "mod_version" to project.version,
                "rusher_wrapper_version" to rusherWrapperVersion,
                "minecraft_version" to stonecutter.current.project,
            )
        }
    }

    get("sourcesJar").dependsOn(":rusher:${stonecutter.current.project}:copyJars")

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    withType<KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
    }

    jar {
        destinationDirectory = rootProject.projectDir.resolve("build/libs")
        from("LICENSE") {
            rename { "${it}_${project.base.archivesName}" }
        }
    }
}