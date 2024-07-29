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

import org.junit.jupiter.api.Test

class FormatCheckWithWorkspaceTest : AbstractFormatCheckTest() {
    @Test
    fun `format a correct message v2`() {
        super.`format a correct message`()
    }

    @Test
    fun `format an incorrect message v2`() {
        super.`format an incorrect message`()
    }

    @Test
    fun `do not format an incorrect message when enforcement is disabled v2`() {
        super.`do not format an incorrect message when enforcement is disabled`()
    }
}
