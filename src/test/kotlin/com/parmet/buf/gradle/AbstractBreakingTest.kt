

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.junit.jupiter.api.Test
import java.nio.file.Path

abstract class AbstractBreakingTest : AbstractBufIntegrationTest() {
    @Test
    fun `breaking schema`() {
        publishRunner().build()
        checkBreaking()
    }

    @Test
    fun `breaking schema with latest-release as version`() {
        publishRunner().build()
        checkBreaking()
    }

    @Test
    fun `normally breaking schema with an ignore`() {
        publishRunner().build()

        breakSchema()

        buildFile.replace("//", "")
        checkRunner().build()
    }

    @Test
    fun `breaking schema fails with latest-release and previousVersion`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Cannot configure $BUF_BREAKING_TASK_NAME against latest release and a previous version.")
    }

    private fun checkBreaking() {
        checkRunner().build()

        buildFile.replace("//", "")

        breakSchema()

        val result = checkRunner().buildAndFail()
        assertThat(result.task(":$BUF_BREAKING_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains(
            """
                Execution failed for task ':bufBreaking'.
                > Some Protobuf files had breaking changes:
            """.trimIndent().osIndependent()
        )
        assertThat(result.output).contains("Previously present message \"BasicMessage\" was deleted from file.")
    }

    private fun breakSchema() {
        val protoFile = protoFile().toFile()
        protoFile.replace("BasicMessage", "BasicMessage2")
    }

    protected abstract fun protoFile(): Path
}
