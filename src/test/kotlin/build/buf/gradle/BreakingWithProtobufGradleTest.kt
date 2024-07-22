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

import org.junit.Ignore
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class BreakingWithProtobufGradleTest : AbstractBreakingTest() {
    override fun protoFile() = Paths.get(protoDir.path, "buf", "test", "v1", "test.proto")

    @Test
    @Ignore
    override fun `normally breaking schema with an ignore`() {
        // Ignore this test until we resolve the future of combining this plugin with protobuf-gradle-plugin
    }

    @Test
    @Ignore
    fun `schema with multi-directory workspace`() {
        // Ignore this test until we resolve the future of combining this plugin with protobuf-gradle-plugin
    }
}
