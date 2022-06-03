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

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.SonatypeHost
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.extra

object ProjectInfo {
    const val name = "Buf Gradle Plugin"
    const val url = "https://github.com/andrewparmet/buf-gradle-plugin"
    const val description = "Buf plugin for Gradle"
}

fun Project.configurePublishing() {
    apply(plugin = "com.vanniktech.maven.publish.base")

    configure<MavenPublishBaseExtension> {
        configure(KotlinJvm(JavadocJar.Empty()))
        publishToMavenCentral(SonatypeHost.DEFAULT)
        signAllPublications()
        pom {
            name.set(ProjectInfo.name)
            description.set(ProjectInfo.description)
            url.set(ProjectInfo.url)
            scm { url.set(ProjectInfo.url) }
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

    if (isRelease()) {
        extra.set("signingInMemoryKey", System.getenv("PGP_KEY")?.replace('$', '\n'))
        extra.set("signingInMemoryPassword", System.getenv("PGP_PASSWORD"))
        extra.set("mavenCentralUsername", System.getenv("OSSRH_USERNAME"))
        extra.set("mavenCentralPassword", System.getenv("OSSRH_PASSWORD"))
    }
}

fun Project.isRelease() =
    !version.toString().endsWith("-SNAPSHOT")
