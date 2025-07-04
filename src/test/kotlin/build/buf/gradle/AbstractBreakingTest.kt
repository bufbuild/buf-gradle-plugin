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

import build.buf.gradle.ImageGenerationSupport.replaceBuildDetails
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Path

abstract class AbstractBreakingTest : AbstractBufIntegrationTest() {
    @Test
    fun `breaking schema`() {
        publishRunner().build()
        checkBreaking()
    }

    @Test
    fun `breaking schema with latest-release as version`() {
        publishRunner().build()
        checkBreaking()
    }

    @ParameterizedTest
    @MethodSource("build.buf.gradle.ImageGenerationSupport#publicationFileExtensionTestCase")
    fun `breaking schema with specified publication file extension`(
        format: String,
        compression: String?,
    ) {
        replaceBuildDetails(format, compression)
        publishRunner().build()
        checkBreaking()
    }

    @Test
    fun `normally breaking schema with an ignore`() {
        publishRunner().build()

        breakSchema()

        buildFile.replace("//", "")
        checkRunner().build()
    }

    @Test
    fun `breaking schema fails with latest-release and previousVersion`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Cannot configure $BUF_BREAKING_TASK_NAME against latest release and a previous version.")
    }

    private fun checkBreaking() {
        checkRunner().build()

        buildFile.replace("//", "")

        breakSchema()

        val result = checkRunner().buildAndFail()
        assertThat(result.task(":$BUF_BREAKING_TASK_NAME")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains(
            """
            Execution failed for task ':bufBreaking'.
            > Some Protobuf files had breaking changes:
            """.trimIndent().osIndependent(),
        )
        assertThat(result.output).contains("Previously present message \"BasicMessage\" was deleted from file.")
    }

    private fun breakSchema() {
        val protoFile = protoFile().toFile()
        protoFile.replace("BasicMessage", "BasicMessage2")
    }

    protected abstract fun protoFile(): Path
}
