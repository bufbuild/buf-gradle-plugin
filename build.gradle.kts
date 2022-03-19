/*
 * Copyright (c) 2021 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    gradlePluginPortal()
}

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm")
    id("com.gradle.plugin-publish") version "0.18.0"
    id("com.diffplug.spotless") version "6.0.5"
}

allprojects {
    spotless {
        kotlin {
            ktlint()
            target("**/*.kt")
        }

        kotlinGradle {
            ktlint()
        }
    }
}

group = "com.parmet"
configurePublishing()
configureStagingRepoTasks()

gradlePlugin {
    isAutomatedPublishing = false

    plugins {
        create("buf") {
            id = "com.parmet.buf"
            implementationClass = "com.parmet.buf.gradle.BufPlugin"
            displayName = ProjectInfo.name
            description = ProjectInfo.description
        }
    }
}

pluginBundle {
    mavenCoordinates {
        group = project.group.toString()
    }
    website = ProjectInfo.url
    vcsUrl = ProjectInfo.url
    description = ProjectInfo.description
    tags = listOf("protobuf", "kotlin", "buf")
}

ext[GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

tasks {
    named("publishPlugins") {
        enabled = isRelease()
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
            jvmTarget = JavaVersion.VERSION_1_8.toString()
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("com.google.truth:truth:1.1.3")
}

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}
