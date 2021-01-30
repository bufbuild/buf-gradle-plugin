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

        val result = checkRunner(projectDir).build()

        assertThat(result.task(":$BUF_LINT_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("must be within a directory \"parmet/buf/test/v1\"")
    }
}
