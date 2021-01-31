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
import java.io.File
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.junit.jupiter.api.Test

class BreakingTest : AbstractBufIntegrationTest() {
    @Test
    fun `breaking schema`() {
        val protoFile = publishSchema()

        buildFile.writeText(
            buildGradle(
                """
                    ${localRepo()}
                    
                    buf {
                      previousVersion = '2319'

                      imageArtifact {
                        groupId = 'foo'
                        artifactId = 'bar'
                      }
                    }
                """.trimIndent()
            )
        )

        checkRunner().build()

        protoFile.writeText(basicProtoFile("BasicMessage2"))

        val result = checkRunner().buildAndFail()
        assertThat(result.task(":bufBreaking")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("Previously present message \"BasicMessage\" was deleted from file.")
    }

    private fun publishSchema(): File {
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

        configFile.writeText(bufYaml())
        val protoFile = protoDir.newFolder("parmet", "buf", "test", "v1").newFile("test.proto")
        protoFile.writeText(basicProtoFile())
        publishRunner().build()

        return protoFile
    }
}
