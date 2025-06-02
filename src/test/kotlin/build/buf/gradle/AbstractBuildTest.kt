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

import build.buf.gradle.ImageGenerationSupport.replaceBuildDetails
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.nio.file.Paths

abstract class AbstractBuildTest : AbstractBufIntegrationTest() {
    @Test
    fun `build image with explicit artifact details`() {
        assertImageGeneration("image.json")
    }

    @Test
    fun `build image with inferred artifact details`() {
        assertImageGeneration("image.json")
    }

    @Test
    fun `build image with no artifact details should fail`() {
        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 0")
    }

    @ParameterizedTest
    @MethodSource("build.buf.gradle.ImageGenerationSupport#publicationFileExtensionTestCase")
    fun `build image with specified publication file extension`(
        format: String,
        compression: String?,
    ) {
        replaceBuildDetails(format, compression)
        val extension = format + (compression?.let { ".$compression" } ?: "")
        assertImageGeneration("image.$extension")
    }

    @Test
    fun `build image with two publications should fail`() {
        val result = buildRunner().buildAndFail()
        assertThat(result.output).contains("Unable to determine image artifact details")
        assertThat(result.output).contains("found 2")
    }

    @Test
    fun `build image with two publications should succeed if details are provided explicitly`() {
        assertImageGeneration("image.json")
    }

    @Test
    fun `build an image reusing an extension number`() {
        val result = buildRunner().buildAndFail()
        val source = listOf("buf", "test", "v1", "test.proto").joinToString(File.separator)
        assertThat(result.output).contains(
            "$source:23:14:extension with tag 1072 for message google.protobuf.MessageOptions already defined at " +
                "validate/validate.proto:17:29",
        )
    }

    @Test
    fun `build with a google dependency`() {
        assertImageGeneration("image.json")
    }

    private fun assertImageGeneration(publicationFileName: String) {
        assertThat(buildRunner().build().task(":$BUF_BUILD_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
        val image = Paths.get(projectDir.path, "build", "bufbuild", publicationFileName).toFile().readText()
        assertThat(image).isNotEmpty()
    }

    private fun buildRunner() = gradleRunner().withArguments(":$BUF_BUILD_TASK_NAME")
}
