buildscript { repositories { gradlePluginPortal() } }

plugins {
    id("io.alcide.gradle-semantic-build-versioning") version "4.2.2"
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "buf-gradle-plugin"
