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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Lightweight tests for plugin compatibility across multiple Gradle versions.
 */
class GradleVersionCompatibilityTest : AbstractBufIntegrationTest() {
    companion object {
        @JvmStatic
        fun allGradleVersions() = GradleVersions.ALL
    }

    @ParameterizedTest(name = "Groovy DSL with Gradle {0}")
    @MethodSource("allGradleVersions")
    fun `buf format works with Groovy DSL`(version: String) {
        assertThat(checkRunner(version).build().task(":$BUF_FORMAT_CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }

    @ParameterizedTest(name = "Kotlin DSL with Gradle {0}")
    @MethodSource("allGradleVersions")
    fun `buf format works with Kotlin DSL`(version: String) {
        assertThat(checkRunner(version).build().task(":$BUF_FORMAT_CHECK_TASK_NAME")?.outcome).isEqualTo(SUCCESS)
    }
}
