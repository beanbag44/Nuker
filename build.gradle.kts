import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.9.1"
    id("maven-publish")
}
val mcVersion = stonecutter.current.project
val modId = project.property("mod_id") as String
val modName = project.property("mod_name") as String
val commandPrefix = project.property("command_prefix") as String
val rusherWrapperVersion = project.property("rusher_wrapper_version")
val yarnMappings = project.property("yarn_mappings") as String
val loaderVersion = project.property("loader_version") as String
val kotlinLoaderVersion = project.property("kotlin_loader_version") as String
val fabricVersion = project.property("fabric_version") as String
val meteorVersion = project.property("meteor_version") as String
val malilibVersion = project.property("malilib_version") as String
val litematicaVersion = project.property("litematica_version") as String
val netherPathfinderVersion = project.property("nether_pathfinder_version") as String
val baritoneVersion = if(stonecutter.current.project == "1.21.2") "1.21.3" else mcVersion

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base.archivesName = "$modId-$mcVersion"

val targetJavaVersion = if (stonecutter.eval(mcVersion, "<1.20.5")) 17 else 21
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
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")

    modImplementation("net.fabricmc:fabric-language-kotlin:$kotlinLoaderVersion")
    include("net.fabricmc:fabric-language-kotlin:$kotlinLoaderVersion")

    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    include("net.fabricmc.fabric-api:fabric-api:$fabricVersion")

    modImplementation("meteordevelopment:baritone:$baritoneVersion-SNAPSHOT")
    if (stonecutter.current.project == "1.21.4") {
        modImplementation("meteordevelopment:meteor-client:$meteorVersion-SNAPSHOT")
    } else {
        modImplementation("meteordevelopment:meteor-client:$meteorVersion")
    }

    modImplementation("maven.modrinth:malilib:$malilibVersion")
    modImplementation("maven.modrinth:litematica:$litematicaVersion")
    modRuntimeOnly("dev.babbaj:nether-pathfinder:$netherPathfinderVersion")
}

tasks {
    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        inputs.property("minecraft_version", mcVersion)
        inputs.property("loader_version", loaderVersion)
        inputs.property("kotlin_loader_version", kotlinLoaderVersion)
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
                "minecraft_version" to mcVersion,
                "loader_version" to loaderVersion,
                "kotlin_loader_version" to kotlinLoaderVersion,
            )
        }

        filesMatching("merge.json") {
            expand(
                "mod_id" to modId,
                "mod_name" to modName,
                "command_prefix" to commandPrefix,
                "mod_version" to project.version,
                "rusher_wrapper_version" to rusherWrapperVersion,
                "minecraft_version" to mcVersion,
            )
        }
    }
    if (stonecutter.current.project != "1.21.2") {
        get("sourcesJar").dependsOn(":rusher:$mcVersion:copyJars")
    }

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