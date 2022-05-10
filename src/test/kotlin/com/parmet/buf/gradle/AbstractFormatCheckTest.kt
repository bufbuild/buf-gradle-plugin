/*
 * Copyright (c) 2022 Andrew Parmet
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

abstract class AbstractFormatCheckTest : AbstractBufIntegrationTest() {
    @Test
    fun `format a correct message`() {
        assertSuccess()
    }

    @Test
    fun `format an incorrect message`() {
        baseAssertBadWhitespace()
    }

    @Test
    fun `do not format an incorrect message when enforcement is disabled`() {
        baseAssertBadWhitespace()

        buildFile.replace("enforceFormat = true", "enforceFormat = false")

        assertSuccess()
    }

    private fun baseAssertBadWhitespace() {
        assertBadWhitespace(
            """
                -message Foo {
                -
                -}
                +message Foo {}
            """.trimIndent()
        )
    }

    protected fun assertBadWhitespace(diff: String) {
        val result = checkRunner().buildAndFail()
        assertThat(result.task(":$BUF_FORMAT_CHECK_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains(diff)
    }

    protected fun assertSuccess() {
        assertThat(checkRunner().build().task(":$CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }
}
