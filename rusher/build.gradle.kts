import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

plugins {
    kotlin("jvm") version "2.1.0"
    id("fabric-loom") version "1.9.1"
}

version = project.property("rusher_wrapper_version") as String
group = project.property("maven_group") as String

base.archivesName = project.property("mod_id") as String + "-" + "${project.property("archives_base_name")}" + "-" + stonecutter.current.project

val rusherhackApi: Configuration by configurations.creating {
    isCanBeResolved = true
    configurations.compileOnly.get().extendsFrom(this)
}

val targetJavaVersion = if (stonecutter.eval(stonecutter.current.version, "<=1.20.6")) 17 else 21

java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    sourceCompatibility = JavaVersion.toVersion(targetJavaVersion)
    targetCompatibility = JavaVersion.toVersion(targetJavaVersion)
}

repositories {
    maven {
        //releases repository will have the latest api version for last stable rusherhack release
        //snapshots will always be the latest api version
        //url = "https://maven.rusherhack.org/releases"
        name = "Rusherhack"
        url = URI("https://maven.rusherhack.org/snapshots")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${stonecutter.current.project}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")
    @Suppress("UsePropertyAccessSyntax") // https://youtrack.jetbrains.com/issue/KT-48300
    implementation(project(path = stonecutter.node.sibling("")!!.getPath(), configuration = "namedElements"))
    rusherhackApi("org.rusherhack:rusherhack-api:${stonecutter.current.project}-SNAPSHOT")
}


tasks {
    val deleteJars by registering(Delete::class) {
        delete(project.fileTree(stonecutter.node.sibling("")!!.projectDir.resolve("src/main/resources")) {
            include("*rusher*.jar")
        })
    }

    val copyJars by registering(Copy::class) {
        dependsOn(deleteJars)
        dependsOn(build)
        dependsOn(validateAccessWidener)
        from(stonecutter.node.sibling("rusher")!!.project.projectDir.resolve("build/libs")) {
            include("*rusher*.jar")
        }

        into(stonecutter.node.sibling("")!!.projectDir.resolve("src/main/resources"))
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("minecraft_version", stonecutter.current.project)
        inputs.property("loader_version", project.property("loader_version"))
        filteringCharset = "UTF-8"

        filesMatching("fabric.mod.json") {
            expand("version" to project.version,
                "minecraft_version" to stonecutter.current.project,
                "loader_version" to project.property("loader_version"),
                "kotlin_loader_version" to project.property("kotlin_loader_version"))
        }

        filesMatching("rusherhack-plugin.json") {
            expand(
                "mod_version" to project.version,
                "minecraft_version" to stonecutter.current.project,
            )
        }
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(targetJavaVersion)
    }

    withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
//        finalizedBy(copyJars)
    }

    jar {
        finalizedBy(copyJars)
    }
}
//tasks.withType<JavaCompile>().configureEach {
//    // ensure that the encoding is set to UTF-8, no matter what the system default is
//    // this fixes some edge cases with special characters not displaying correctly
//    // see http://yodaconditions.net/blog/fix-for-java-file-encoding-problems-with-gradle.html
//    // If Javadoc is generated, this must be specified in that task too.
//    options.encoding = "UTF-8"
//    options.release.set(javaVersion.toInt())
//    dependsOn(":common:build")
//}
//
//tasks.withType<KotlinCompile>().configureEach {
//    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(javaVersion))
//}

