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

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import java.nio.file.Paths
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test

class PublishTest : AbstractBufIntegrationTest() {
    @Test
    fun `publishing schema with explicit artifact details`() {
        buildFile.writeText(
            buildGradle(
                """
                    apply plugin: 'maven-publish'
                    
                    publishing { ${localRepo()} }
                    
                    buf {
                      publishSchema = true
                      imageArtifact {
                        groupId = 'foo'
                        artifactId = 'bar'
                        version = '2319'
                      }
                    }
                """.trimIndent()
            )
        )

        assertImagePublication("bar")
    }

    @Test
    fun `publishing schema with inferred artifact details`() {
        buildFile.writeText(
            buildGradle(
                """
                    apply plugin: 'maven-publish'
                    
                    publishing { ${localRepo()} }
                    
                    publishing {
                      publications {
                        maven(MavenPublication) {
                          groupId = 'foo'
                          artifactId = 'bar'
                          version = '2319'
                          from components.java
                        }
                      }
                    }
                    
                    buf {
                      publishSchema = true
                    }
                """.trimIndent()
            )
        )

        assertImagePublication("bar-bufbuild")
    }

    private fun assertImagePublication(artifactId: String) {
        configFile.writeText(bufYaml())

        protoDir.newFolder("parmet", "buf", "test", "v1")
            .newFile("test.proto")
            .writeText(basicProtoFile())

        assertThat(publishRunner().build().task(":publish")?.outcome).isEqualTo(SUCCESS)

        val builtImage = Paths.get(projectDir.path, "build", "bufbuild", "image.json")
        val publishedImage = Paths.get(projectDir.path, "build", "repos", "test", "foo", artifactId, "2319", "$artifactId-2319.json")

        assertThat(publishedImage.toFile().readText()).isEqualTo(builtImage.toFile().readText())
    }
}
