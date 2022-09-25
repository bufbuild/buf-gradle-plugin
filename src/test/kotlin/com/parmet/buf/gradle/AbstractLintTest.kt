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
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import java.io.File

abstract class AbstractLintTest : LintTestUtilities, AbstractBufIntegrationTest() {
    @Test
    fun `lint a basic correct message with default config`() {
        assertSuccess()
    }

    @Test
    fun `lint a basic correct message with default config before java plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a basic correct message with default config and the kotlin jvm plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a basic correct message with default config and the kotlin android plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a basic incorrect message with bad enum`() {
        assertBadEnumSuffix()
    }

    @Test
    fun `lint a file with a google dependency`() {
        assertSuccess()
    }

    private fun assertBadEnumSuffix() {
        val result = checkRunner().buildAndFail()
        assertThat(result.task(":$BUF_LINT_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains(
            """
                Execution failed for task ':bufLint'.
                > Some Protobuf files had lint violations:
            """.trimIndent()
        )
        assertThat(result.output).contains("Enum zero value name \"TEST_FOO\" should be suffixed with \"_UNSPECIFIED\"")
    }
}

interface LintTestUtilities : IntegrationTest {
    fun assertSuccess() {
        assertThat(checkRunner().build().task(":$CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }
}
