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
    alias(libs.plugins.publishing)
}

group = "build.buf"
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

val targetJavaVersion = JavaVersion.VERSION_1_8
val targetKotlinVersion = "1.6"

configure<JavaPluginExtension> {
    sourceCompatibility = targetJavaVersion
    targetCompatibility = targetJavaVersion
}

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
            languageVersion = targetKotlinVersion
            apiVersion = targetKotlinVersion
            if (JavaVersion.current().isJava9Compatible) {
                freeCompilerArgs += "-Xjdk-release=1.8"
            }
        }
    }

    withType<JavaCompile> {
        if (JavaVersion.current().isJava9Compatible) {
            doFirst {
                options.compilerArgs = listOf("--release", "8")
            }
        }
        sourceCompatibility = targetJavaVersion.toString()
        targetCompatibility = targetJavaVersion.toString()
    }
}
