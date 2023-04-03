

import io.github.gradlenexus.publishplugin.NexusPublishExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.SigningExtension

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

object ProjectInfo {
    const val name = "Buf Gradle Plugin"
    const val url = "https://github.com/andrewparmet/buf-gradle-plugin"
    const val description = "Buf plugin for Gradle"
}

fun Project.configurePublishing() {
    apply(plugin = "io.github.gradle-nexus.publish-plugin")

    configure<NexusPublishExtension> {
        repositories {
            sonatype {
                username.set(Remote.username)
                password.set(Remote.password)
            }
        }
        packageGroup.set("com.parmet")
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }

    if (isRelease()) {
        apply(plugin = "signing")

        configure<SigningExtension> {
            useInMemoryPgpKeys(Pgp.key, Pgp.password)

            the<PublishingExtension>().publications.withType<MavenPublication> {
                standardPom()
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
}

private fun MavenPublication.standardPom() {
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
        }
    }
}

fun Project.isRelease() = !version.toString().endsWith("-SNAPSHOT")
