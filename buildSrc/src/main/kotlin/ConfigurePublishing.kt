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

import io.codearte.gradle.nexus.NexusStagingExtension
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
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

    val url = "https://oss.sonatype.org/service/local/staging/deploy/maven2"
}

object ProjectInfo {
    const val name = "Buf Gradle Plugin"
    const val url = "https://github.com/andrewparmet/buf-gradle-plugin"
    const val description = "Buf plugin for Gradle"
}

fun MavenPublication.standardPom() {
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

fun Project.configurePublishing() {
    apply(plugin = "maven-publish")

    configure<PublishingExtension> {
        repositories {
            if (isRelease()) {
                maven {
                    name = "remote"
                    setUrl(Remote.url)
                    credentials {
                        username = Remote.username
                        password = Remote.password
                    }
                }
            }
        }
    }

    configure<JavaPluginExtension> {
        withSourcesJar()
    }

    tasks.register<Jar>("javadocJar") {
        from("$rootDir/README.md")
        archiveClassifier.set("javadoc")
    }

    configure<PublishingExtension> {
        publications {
            create<MavenPublication>("main") {
                from(components.getByName("java"))
                artifact(tasks.getByName("javadocJar"))

                artifactId = project.name
                version = project.version.toString()
                groupId = project.group.toString()
            }
        }
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

    tasks.register("publishToRemote") {
        enabled = isRelease()
        group = "publishing"

        if (enabled) {
            dependsOn(
                tasks.withType<PublishToMavenRepository>()
                    .matching { it.repositoryIs("remote") }
            )
        }
    }
}

fun PublishToMavenRepository.repositoryIs(name: String) =
    repository == project.the<PublishingExtension>().repositories.getByName(name)

fun Project.configureStagingRepoTasks() {
    if (isRelease()) {
        apply(plugin = "io.codearte.nexus-staging")

        configure<NexusStagingExtension> {
            username = Remote.username
            password = Remote.password
            packageGroup = "com.parmet"
            numberOfRetries = 50
        }
    } else {
        tasks.register("closeAndReleaseRepository")
    }
}
