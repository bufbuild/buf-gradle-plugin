// Copyright 2024 Buf Technologies, Inc.
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
import org.junit.jupiter.api.assertThrows

class BufSupportTest {
    @Test
    fun `handleResult includes stderr when stdout is empty and custom error message is specified`() {
        val thrown =
            assertThrows<IllegalStateException> {
                handleResult(
                    ProcessRunner.Result(
                        emptyList(),
                        1,
                        "".toByteArray(),
                        "error that would be hidden".toByteArray(),
                    ),
                ) { "foo" }
            }

        assertThat(thrown).hasMessageContaining("error that would be hidden")
    }
}
