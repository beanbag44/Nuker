import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("maven-publish")
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

configurations {
    configurations.create("rusherhackApi")
    configurations.getByName("rusherhackApi").isCanBeResolved = true
    compileOnly.get().extendsFrom(configurations.getByName("rusherhackApi"))
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = URI("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    maven {
        //releases repository will have the latest api version for last stable rusherhack release
        //snapshots will always be the latest api version
        //url = "https://maven.rusherhack.org/releases"
        name = "Rusherhack"
        url = URI("https://maven.rusherhack.org/snapshots")
    }
    maven("https://maven.meteordev.org/releases") // Meteor
    maven("https://maven.meteordev.org/snapshots") // Baritone/ meteor
    maven("https://babbaj.github.io/maven/") //Nether Pathfinder
}

loom {
    accessWidenerPath = file("src/main/resources/nuker.accesswidener")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    // GUI stuff
//    implementation("org.lwjgl:lwjgl-nuklear:3.3.1")
//    runtimeOnly("org.lwjgl:lwjgl-nuklear:3.3.1:natives-linux")
//    runtimeOnly("org.lwjgl:lwjgl-nuklear:3.3.1:natives-windows")
//    runtimeOnly("org.lwjgl:lwjgl-nuklear:3.3.1:natives-macos")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    modImplementation("dev.babbaj:nether-pathfinder:1.4.1")
    modImplementation("meteordevelopment:baritone:${project.property("minecraft_version")}-SNAPSHOT")
    modImplementation("meteordevelopment:meteor-client:${project.property("meteor_version")}")
    modImplementation("maven.modrinth:malilib:0.19.0")
    modImplementation("maven.modrinth:litematica:0.18.1")
    configurations.getByName("rusherhackApi")("org.rusherhack:rusherhack-api:1.20.6-SNAPSHOT")
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
    filesMatching("rusherhack-plugin.json") {
        expand("mod_version" to project.version)
    }
}

tasks.withType<JavaCompile>().configureEach {
    // ensure that the encoding is set to UTF-8, no matter what the system default is
    // this fixes some edge cases with special characters not displaying correctly
    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
    // If Javadoc is generated, this must be specified in that task too.
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

// configure the maven publication
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.property("archives_base_name") as String
            from(components["java"])
        }
    }

    // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
    repositories {
        // Add repositories to publish to here.
        // Notice: This block does NOT have the same function as the block in the top level.
        // The repositories here will be used for publishing your artifact, not for
        // retrieving dependencies.
    }
}
