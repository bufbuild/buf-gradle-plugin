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
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class LintWithProtobufGradleTest : ConfigOverrideableLintTests, AbstractLintTest() {
    @Test
    fun `lint with protobuf plugin applied after buf plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a separate protobuf source directory through the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with a protobuf dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with a protobuf dependency and a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with an implementation dependency and a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with an implementation dependency and a lint config with the protobuf-gradle-plugin`() {
        val result = gradleRunner().withArguments(":$CHECK_TASK_NAME").buildAndFail()
        assertThat(result.output).contains("Enum zero value name \"BROKEN_ENUM_NONSPECIFIED\" should be suffixed with \"_UNSPECIFIED\"")

        Paths.get(projectDir.path, "buf.yaml").toFile().replace("#", "")

        assertSuccess()
    }
}
