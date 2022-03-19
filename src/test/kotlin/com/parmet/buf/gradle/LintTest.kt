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
import java.io.File

class LintTest : AbstractBufIntegrationTest() {
    @Test
    fun `linting a basic correct message with default config`() {
        assertSuccess()
    }

    @Test
    fun `linting a basic incorrect message with wrong location`() {
        assertLocationFailure()
    }

    @Test
    fun `linting with a config in default location`() {
        assertSuccess()
    }

    @Test
    fun `linting with a file location config override`() {
        try {
            assertSuccess()
        } catch (t: Throwable) {
            File(projectDir, "build/bufbuild").walkTopDown().forEach {
                println(it)
                if (it.name == "buf.yaml") {
                    println(it.readText())
                }
            }
            throw t
        }
    }

    @Test
    fun `linting with a dependency config override`() {
        assertSuccess()
    }

    @Test
    fun `linting with a dependency config override fails with two files`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("buf-1.yaml")
        assertThat(result.output).contains("buf-2.yaml")
    }

    @Test
    fun `linting with a dependency config override fails with no files`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("had []")
    }

    @Test
    fun `linting with a file and dependency config override fails`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("config file location and a dependency; pick one")
    }

    @Test
    fun `linting a separate protobuf source directory through the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `linting a file with a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `linting a file with a protobuf dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `linting a file with a protobuf dependency and a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    private fun assertLocationFailure() {
        val result = checkRunner().buildAndFail()
        assertThat(result.task(":bufLint")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("must be within a directory \"parmet/buf/test/v1\"")
    }

    private fun assertSuccess() {
        assertThat(checkRunner().build().task(":check")?.outcome).isEqualTo(SUCCESS)
    }
}
