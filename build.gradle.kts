import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET
import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

repositories {
    gradlePluginPortal()
    mavenCentral()
}

plugins {
    `kotlin-dsl`
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.mavenPublish)
    alias(libs.plugins.osDetector)
    alias(libs.plugins.pluginPublish)
    alias(libs.plugins.spotless)
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

val dependabotConfig = configurations.create("dependabotConfig")
val protoc: Configuration by configurations.creating

dependencies {
    // Trigger dependabot on new releases.
    dependabotConfig(libs.bufbuild)
    dependabotConfig(libs.androidGradlePlugin)
    dependabotConfig(libs.protobufGradlePlugin)

    implementation(libs.jacksonDataformatYaml)
    implementation(libs.jacksonModuleKotlin)
    implementation(libs.versioncompare)

    testImplementation(platform(libs.junit))
    testImplementation(libs.assertj)
    testImplementation(libs.guava)
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.platform:junit-platform-launcher")
    protoc("com.google.protobuf:protoc:${libs.versions.protoc.get()}:${osdetector.classifier}@exe")
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

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
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

        systemProperty("protoc.path", protoc.asPath)
    }

    withType<KotlinCompile> {
        compilerOptions {
            allWarningsAsErrors = true
        }
    }
}

buildConfig {
    useKotlinOutput { topLevelConstants = true }
    packageName.set("build.buf.gradle")
    buildConfigField("String", "DEFAULT_BUF_VERSION", "\"${libs.versions.bufbuild.get()}\"")

    sourceSets.getByName("test") {
        buildConfigField("String", "PROTOBUF_VERSION", "\"${libs.versions.protoc.get()}\"")
        buildConfigField("String", "KOTLIN_VERSION", "\"${libs.versions.kotlin.get()}\"")
        buildConfigField("String", "PROTOBUF_GRADLE_PLUGIN_VERSION", "\"${libs.versions.protobufGradlePlugin.get()}\"")
        buildConfigField("String", "ANDROID_GRADLE_PLUGIN_VERSION", "\"${libs.versions.androidGradlePlugin.get()}\"")
    }
}
