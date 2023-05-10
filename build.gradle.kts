import com.gradle.publish.PublishTask.GRADLE_PUBLISH_KEY
import com.gradle.publish.PublishTask.GRADLE_PUBLISH_SECRET
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension
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
    alias(libs.plugins.pluginNexusPublish)
}

private object ProjectInfo {
    const val name = "Buf Gradle Plugin"
    const val url = "https://github.com/bufbuild/buf-gradle-plugin"
    const val description = "Buf plugin for Gradle"
}

group = "build.buf"
val releaseVersion = project.findProperty("releaseVersion") as String?
// Default to snapshot versioning for local publishing.
version = releaseVersion ?: "0.0.0-SNAPSHOT"

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
            id = "build.buf"
            implementationClass = "build.buf.gradle.BufPlugin"
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

configure<JavaPluginExtension> {
    withSourcesJar()
}

/* Publishing */
apply(plugin = "io.github.gradle-nexus.publish-plugin")
configure<io.github.gradlenexus.publishplugin.NexusPublishExtension> {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(Remote.username)
            password.set(Remote.password)
        }
    }
    packageGroup.set("build.buf")
}

apply(plugin = "signing")
configure<SigningExtension> {
    if (isRelease()) {
        useInMemoryPgpKeys(Pgp.key, Pgp.password)
        the<PublishingExtension>().publications.withType<MavenPublication> {
            pom {
                name.set(ProjectInfo.name)
                description.set(ProjectInfo.description)
                url.set(ProjectInfo.url)
                scm {
                    url.set(ProjectInfo.url)
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
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
                        organization.set("Buf Techonologies, Inc.")
                        organizationUrl.set("https://buf.build")
                    }
                }
            }
            sign(this)
        }
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("main") {
            from(components.getByName("java"))
            artifactId = project.name
            version = project.version.toString()
            groupId = project.group.toString()
        }
    }
}

fun isRelease() = !version.toString().endsWith("-SNAPSHOT")

private object Pgp {
    val key by lazy {
        System.getenv("PGP_KEY")?.replace('$', '\n')
    }

    val password by lazy {
        System.getenv("PGP_PASSWORD")
    }
}

private object Remote {
    val username by lazy {
        System.getenv("OSSRH_USERNAME")
    }

    val password by lazy {
        System.getenv("OSSRH_PASSWORD")
    }
}
