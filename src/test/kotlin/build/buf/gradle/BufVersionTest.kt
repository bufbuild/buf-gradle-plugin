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
import org.junit.jupiter.api.Test

class BufVersionTest : AbstractBufIntegrationTest() {
    @Test
    fun `buf version can be specified by the extension`() {
        val result = gradleRunner().withArguments("printBufVersion", "-PbufVersion=asdf").build()

        val versionLine = result.output.lines().single { it.startsWith("Resolved") }

        assertThat(versionLine).isEqualTo("Resolved Buf tool version: asdf")
    }
}
