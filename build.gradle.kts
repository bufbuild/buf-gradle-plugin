import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.gradle.kotlin.kotlin-dsl.base") version "1.3.6"
    `maven-publish`
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true
        jvmTarget = "1.8"
    }
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}

group = "com.parmet"
description = "Buf plugin for Gradle"

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "buf-gradle-plugin"
            version = project.version.toString()

            from(components["java"])
        }
    }
}

gradlePlugin {
    setAutomatedPublishing(true)

    plugins {
        create("buf") {
            id = "com.parmet.buf"
            implementationClass = "com.parmet.buf.gradle.BufPlugin"
            displayName = "Buf Gradle Plugin"
            description = project.description
        }
    }
}

pluginBundle {
    mavenCoordinates {
        group = project.group.toString()
    }
    website = "https://github.com/andrewparmet/buf-gradle-plugin"
    vcsUrl = "https://github.com/andrewparmet/buf-gradle-plugin"
    description = project.description
    tags = listOf("protobuf", "kotlin", "buf")
}

tasks.named("publishPlugins") {
    enabled = !version.toString().endsWith("-SNAPSHOT")
}
