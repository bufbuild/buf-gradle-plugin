// Copyright 2023 Buf Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package build.buf.gradle

import org.assertj.core.api.Assertions.assertThat
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
        assertBadWhitespace()
    }

    @Test
    fun `do not format an incorrect message when enforcement is disabled`() {
        assertBadWhitespace()

        buildFile.replace("enforceFormat = true", "enforceFormat = false")

        assertSuccess()
    }

    protected fun assertBadWhitespace() {
        val result = checkRunner().buildAndFail()
        assertThat(result.task(":$BUF_FORMAT_CHECK_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output)
            .contains(
                """
                    |  -message Foo {
                    |  -
                    |  -}
                    |  +message Foo {}
                    |  
                    |  Run './gradlew :bufFormatApply' to fix these violations.
                """.trimMargin(),
            )
    }

    protected fun assertSuccess() {
        assertThat(checkRunner().build().task(":$CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }
}
