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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class BuildWithWorkspaceTest : AbstractBuildTest() {
    @Test
    fun `build image with explicit artifact details v2`() {
        super.`build image with explicit artifact details`()
    }

    @Test
    fun `build image with inferred artifact details v2`() {
        super.`build image with inferred artifact details`()
    }

    @Test
    fun `build image with no artifact details should fail v2`() {
        super.`build image with no artifact details should fail`()
    }

    @ParameterizedTest
    @MethodSource("build.buf.gradle.ImageGenerationSupport#publicationFileExtensionTestCase")
    fun `build image with specified publication file extension v2`(
        format: String,
        compression: String?,
    ) {
        super.`build image with specified publication file extension`(format, compression)
    }

    @Test
    fun `build image with two publications should fail v2`() {
        super.`build image with two publications should fail`()
    }

    @Test
    fun `build image with two publications should succeed if details are provided explicitly v2`() {
        super.`build image with two publications should succeed if details are provided explicitly`()
    }
}
