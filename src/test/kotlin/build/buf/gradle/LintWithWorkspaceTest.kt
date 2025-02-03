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

class LintWithWorkspaceTest :
    AbstractLintTest(),
    NonProtobufGradlePluginLintTests {
    @Test
    fun `lint with a config in workspace`() {
        assertSuccess()
    }

    @Test
    fun `lint a basic correct message with default config v2`() {
        super.`lint a basic correct message with default config`()
    }

    @Test
    fun `lint a basic correct message with default config before java plugin v2`() {
        super.`lint a basic correct message with default config before java plugin`()
    }

    @Test
    fun `lint a basic correct message with default config and the kotlin jvm plugin v2`() {
        super.`lint a basic correct message with default config and the kotlin jvm plugin`()
    }

    @Test
    fun `lint a basic correct message with default config and the kotlin android plugin v2`() {
        super.`lint a basic correct message with default config and the kotlin android plugin`()
    }

    @Test
    fun `lint a basic incorrect message with bad enum v2`() {
        super.`lint a basic incorrect message with bad enum`()
    }

    @Test
    fun `lint a file with a google dependency v2`() {
        super.`lint a file with a google dependency`()
    }
}
