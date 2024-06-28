import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET
import com.vanniktech.maven.publish.SonatypeHost
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
    alias(libs.plugins.mavenPublish)
}

group = "build.buf"

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

object ProjectInfo {
    const val NAME = "Buf Gradle Plugin"
    const val URL = "https://github.com/bufbuild/buf-gradle-plugin"
    const val DESCRIPTION = "Buf plugin for Gradle"
}

fun isRelease() = !version.toString().endsWith("-SNAPSHOT")

gradlePlugin {
    website.set(ProjectInfo.URL)
    vcsUrl.set(ProjectInfo.URL)
    plugins {
        create("buf") {
            id = "build.buf"
            implementationClass = "build.buf.gradle.BufPlugin"
            displayName = ProjectInfo.NAME
            description = ProjectInfo.DESCRIPTION
            tags.set(listOf("protobuf", "kotlin", "buf"))
        }
    }
}

ext[GRADLE_PUBLISH_KEY] = System.getenv("GRADLE_PORTAL_PUBLISH_KEY")
ext[GRADLE_PUBLISH_SECRET] = System.getenv("GRADLE_PORTAL_PUBLISH_SECRET")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.S01)
    signAllPublications()
    pom {
        name.set(ProjectInfo.NAME)
        description.set(ProjectInfo.DESCRIPTION)
        url.set(ProjectInfo.URL)
        scm {
            url.set(ProjectInfo.URL)
        }
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("Andrew Parmet")
                name.set("Andrew Parmet")
                email.set("andrew@parmet.com")
            }
            developer {
                id.set("bufbuild")
                name.set("Buf")
                email.set("dev@buf.build")
                url.set("https://buf.build")
                organization.set("Buf Technologies, Inc.")
                organizationUrl.set("https://buf.build")
            }
        }
    }
}

tasks {
    // Only enable publishing to the Gradle Portal for release builds.
    named("publishPlugins") {
        enabled = isRelease()
    }

    withType<Test> {
        useJUnitPlatform()
    }

    withType<KotlinCompile> {
        kotlinOptions {
            allWarningsAsErrors = true
        }
    }
}
