import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    gradlePluginPortal()
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.spotless)
    alias(libs.plugins.animalsniffer)
}

group = "com.parmet"
version = "0.0.0-SNAPSHOT"
configurePublishing()

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

dependencies {
    signature(libs.java8Signature) { artifact { type = "signature" } }

    testImplementation(libs.junit)
    testImplementation(libs.truth)
}

configure<JavaPluginExtension> {
    sourceCompatibility = targetJavaVersion
    targetCompatibility = targetJavaVersion
}

gradlePlugin {
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
    website = ProjectInfo.url
    vcsUrl = ProjectInfo.url
    description = ProjectInfo.description
    tags = listOf("protobuf", "kotlin", "buf")
}

ext[GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

val targetJavaVersion = JavaVersion.VERSION_1_8

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
            jvmTarget = targetJavaVersion.toString()
        }
    }
}
