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
import org.junit.jupiter.api.Test

class ConfigurationTest : AbstractBufIntegrationTest() {
    @Test
    fun `project cannot use both workspaces and the protobuf-gradle-plugin, protobuf applied first`() {
        assertFailure()
    }

    @Test
    fun `project cannot use both workspaces and the protobuf-gradle-plugin, protobuf applied second`() {
        assertFailure()
    }

    @Test
    fun `project can use both buf-yaml and the protobuf-gradle-plugin, protobuf applied first`() {
        gradleRunner().withArguments(":tasks").build()
    }

    @Test
    fun `project can use both buf-yaml and the protobuf-gradle-plugin, protobuf applied second`() {
        gradleRunner().withArguments(":tasks").build()
    }

    private fun assertFailure() {
        val result = gradleRunner().withArguments(":tasks").buildAndFail()
        assertThat(result.output).contains("cannot use both the protobuf-gradle-plugin and a Buf workspace")
    }
}
