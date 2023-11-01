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

    private fun buildRunner() = gradleRunner().withArguments(":$BUF_BUILD_TASK_NAME")
}
