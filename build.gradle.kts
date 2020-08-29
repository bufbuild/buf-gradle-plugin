import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72"
    id("com.gradle.plugin-publish") version "0.11.0"
    id("org.gradle.kotlin.kotlin-dsl.base") version "1.3.6"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())
}
