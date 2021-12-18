/*
 * Copyright (c) 2021 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.FAILED
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Paths

class BreakingTest : AbstractBufIntegrationTest() {
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

    @Test
    fun `breaking schema fails with latest-release and previousVersion`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Cannot configure bufBreaking against latest release and a previous version.")
    }

    @Test
    fun `schema with multi-directory workspace`() {
        publishRunner().build()
        buildFile.replace("//", "")
        checkRunner().build()
    }

    private fun checkBreaking() {
        checkRunner().build()

        buildFile.replace("//", "")

        val protoFile = Paths.get(protoDir.path, "parmet", "buf", "test", "v1", "test.proto").toFile()
        protoFile.replace("BasicMessage", "BasicMessage2")

        val result = checkRunner().buildAndFail()
        assertThat(result.task(":bufBreaking")?.outcome).isEqualTo(FAILED)
        assertThat(result.output).contains("Previously present message \"BasicMessage\" was deleted from file.")
    }

    private fun File.replace(oldValue: String, newValue: String) {
        writeText(readText().replace(oldValue, newValue))
    }
}
