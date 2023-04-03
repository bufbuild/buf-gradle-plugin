

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import java.nio.file.Paths

abstract class AbstractBuildTest : AbstractBufIntegrationTest() {
    @Test
    fun `build image with explicit artifact details`() {
        assertImageGeneration()
    }

    @Test
    fun `build image with inferred artifact details`() {
        assertImageGeneration()
    }

    @Test
    fun `build image with no artifact details should fail`() {
        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 0")
    }

    @Test
    fun `build image with two publications should fail`() {
        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 2")
    }

    @Test
    fun `build image with two publications should succeed if details are provided explicitly`() {
        assertImageGeneration()
    }

    private fun assertImageGeneration() {
        assertThat(buildRunner().build().task(":$BUF_BUILD_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
        val image = Paths.get(projectDir.path, "build", "bufbuild", "image.json").toFile().readText()
        assertThat(image).isNotEmpty()
    }

    private fun buildRunner() =
        gradleRunner().withArguments(":$BUF_BUILD_TASK_NAME")
}
