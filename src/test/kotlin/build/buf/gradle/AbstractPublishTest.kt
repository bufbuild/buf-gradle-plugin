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
