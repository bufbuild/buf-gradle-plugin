

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import java.nio.file.Paths

abstract class AbstractPublishTest : AbstractBufIntegrationTest() {
    @Test
    fun `publish schema with explicit artifact details`() {
        assertImagePublication("bar")
    }

    @Test
    fun `publish schema with inferred artifact details`() {
        assertImagePublication("bar-bufbuild")
    }

    private fun assertImagePublication(artifactId: String) {
        assertThat(publishRunner().build().task(":publish")?.outcome).isEqualTo(SUCCESS)

        val builtImage = Paths.get(projectDir.path, "build", "bufbuild", "image.json")
        val publishedImage = Paths.get(projectDir.path, "build", "repos", "test", "foo", artifactId, "2319", "$artifactId-2319.json")

        assertThat(publishedImage.toFile().readText()).isEqualTo(builtImage.toFile().readText())
        assertThat(publishedImage.toFile().readText()).contains("BasicMessage")
    }
}
