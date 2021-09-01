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
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test

class LintTest : AbstractBufIntegrationTest() {
    @Test
    fun `linting a basic correct message`() {
        buildFile.writeText(buildGradle())
        configFile.writeText(bufYaml())

        protoDir.newFolder("parmet", "buf", "test", "v1")
            .newFile("test.proto")
            .writeText(basicProtoFile())

        assertThat(checkRunner().build().task(":check")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `linting a basic incorrect message with wrong location`() {
        buildFile.writeText(buildGradle())
        configFile.writeText(bufYaml())

        protoDir.newFile("test.proto").writeText(basicProtoFile())

        assertLocationFailure()
    }

    @Test
    fun `linting with a file location config override`() {
        setUpWithFailure()

        projectDir.newFolder("subdir").newFile("buf.yaml").writeText(bufYaml())

        buildFile.writeText(
            buildGradle(
                """
                    buf {
                      configFileLocation = project.file("subdir/buf.yaml")
                    }
                """.trimIndent()
            )
        )

        assertThat(checkRunner().build().task(":check")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `linting with a dependency config override`() {
        setUpWithFailure()

        projectDir.newFolder("subdir").newFile("buf.yaml").writeText(bufYaml())

        buildFile.writeText(
            buildGradle(
                """
                    dependencies {
                      buf(fileTree('subdir') { include '*.yaml' })
                    }
                """.trimIndent()
            )
        )

        assertThat(checkRunner().build().task(":check")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `linting with a dependency config override fails with two files`() {
        writeProto()

        val dir = projectDir.newFolder("subdir")
        dir.newFile("buf-1.yaml").writeText(bufYaml())
        dir.newFile("buf-2.yaml").writeText(bufYaml())

        buildFile.writeText(
            buildGradle(
                """
                    dependencies {
                      buf(fileTree('subdir') { include '*.yaml' })
                    }
                """.trimIndent()
            )
        )

        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("buf-1.yaml")
        assertThat(result.output).contains("buf-2.yaml")
    }

    @Test
    fun `linting with a dependency config override fails with no files`() {
        writeProto()

        projectDir.newFolder("subdir")

        buildFile.writeText(
            buildGradle(
                """
                    dependencies {
                      buf(fileTree('subdir') { include '*.yaml' })
                    }
                """.trimIndent()
            )
        )

        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("had []")
    }

    @Test
    fun `linting with a file and dependency config override fails`() {
        writeProto()

        projectDir.newFolder("subdir").newFile("buf.yaml").writeText(bufYaml())

        buildFile.writeText(
            buildGradle(
                """
                    buf {
                      configFileLocation = project.file("subdir/buf.yaml")
                    }

                    dependencies {
                      buf(fileTree('subdir') { include '*.yaml' })
                    }
                """.trimIndent()
            )
        )

        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("config file location and a dependency; pick one")
    }

    private fun setUpWithFailure() {
        println("Setting up failing buf run")
        buildFile.writeText(buildGradle())
        writeProto("extra-path")
        assertLocationFailure()
        protoDir.walkTopDown().forEach { it.delete() }
        println("Setting up Gradle clean")
        gradleRunner().withArguments("clean").build()
        println("Finished executing failed buf run")
        writeProto()
    }

    private fun writeProto(extraPath: String? = null) {
        protoDir.newFolder(*listOfNotNull(extraPath, "parmet", "buf", "test", "v1").toTypedArray())
            .newFile("test.proto")
            .writeText(basicProtoFile())
    }

    private fun assertLocationFailure() {
        val result = checkRunner().buildAndFail()
        assertThat(result.task(":bufLint")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("must be within a directory \"parmet/buf/test/v1\"")
    }
}
