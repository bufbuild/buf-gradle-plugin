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

class BuildTest : AbstractBufIntegrationTest() {
    @Test
    fun `build image with explicit artifact details`() {
        buildFile.writeText(
            buildGradle(
                """
                    $publishSchema
                    
                    $imageArtifact
                """.trimIndent()
            )
        )

        assertImageGeneration()
    }

    @Test
    fun `build image with inferred artifact details`() {
        buildFile.writeText(
            buildGradle(
                """
                    $publishSchema
                    
                    publishing {
                      $publication
                    }
                """.trimIndent()
            )
        )

        assertImageGeneration()
    }

    @Test
    fun `build image with no artifact details should fail`() {
        buildFile.writeText(buildGradle(publishSchema))

        prepareProject()

        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 0")
    }

    @Test
    fun `build image with two publications should fail`() {
        buildFile.writeText(
            buildGradle(
                """
                    $publishSchema
                    
                    publishing {
                      $publication
                      
                      $publication2
                    }
                """.trimIndent()
            )
        )

        prepareProject()

        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 2")
    }

    @Test
    fun `build image with two publications should succeed if details are provided explicitly`() {
        buildFile.writeText(
            buildGradle(
                """
                    $publishSchema
                    
                    publishing {
                      $publication
                      
                      $publication2
                    }
                    
                    $imageArtifact
                """.trimIndent()
            )
        )

        assertImageGeneration()
    }

    private fun prepareProject() {
        protoDir.newFolder("parmet", "buf", "test", "v1")
            .newFile("test.proto")
            .writeText(basicProtoFile())
    }

    private fun assertImageGeneration() {
        prepareProject()

        assertThat(buildRunner().build().task(":bufBuild")?.outcome).isEqualTo(SUCCESS)
        val image = Paths.get(projectDir.path, "build", "bufbuild", "image.json").toFile().readText()
        assertThat(image).isNotEmpty()
    }

    private fun buildRunner() =
        gradleRunner().withArguments(":bufBuild")
}
