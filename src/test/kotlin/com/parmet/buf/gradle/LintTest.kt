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
import com.parmet.buf.gradle.BufPlugin.Companion.BUF_LINT_TASK_NAME
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
            .writeText(
                """
                    syntax = "proto3";
    
                    package parmet.buf.test.v1;
    
                    message BasicMessage {}
                """.trimIndent()
            )

        assertThat(checkRunner(projectDir).build().task(":check")?.outcome).isEqualTo(SUCCESS)
    }

    @Test
    fun `linting a basic incorrect message`() {
        buildFile.writeText(buildGradle())
        configFile.writeText(bufYaml())

        protoDir.newFile("test.proto")
            .writeText(
                """
                    syntax = "proto3";
    
                    package parmet.buf.test.v1;
    
                    message BasicMessage {}
                """.trimIndent()
            )

        val result = checkRunner(projectDir).buildAndFail()

        assertThat(result.task(":$BUF_LINT_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("must be within a directory \"parmet/buf/test/v1\"")
    }
}
