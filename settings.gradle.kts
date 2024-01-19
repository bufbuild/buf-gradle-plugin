buildscript { repositories { gradlePluginPortal() } }

plugins {
    id("io.alcide.gradle-semantic-build-versioning") version "4.2.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "buf-gradle-plugin"
