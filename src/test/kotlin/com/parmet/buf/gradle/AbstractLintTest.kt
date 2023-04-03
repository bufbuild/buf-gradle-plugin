

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test

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
            """.trimIndent().osIndependent()
        )
        assertThat(result.output).contains("Enum zero value name \"TEST_FOO\" should be suffixed with \"_UNSPECIFIED\"")
    }
}

interface LintTestUtilities : IntegrationTest {
    fun assertSuccess() {
        assertThat(checkRunner().build().task(":$CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }
}
