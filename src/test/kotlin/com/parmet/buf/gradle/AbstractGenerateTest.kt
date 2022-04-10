/*
 * Copyright (c) 2022 Andrew Parmet
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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.concurrent.TimeUnit

abstract class AbstractGenerateTest : AbstractBufIntegrationTest() {
    @AfterEach
    fun cleanUp() {
        println("ls -l".runCommand(File(projectDir, "build/$BUF_GENERATED_DIR")))
        println("ls -l".runCommand(File(projectDir, "build/$BUF_GENERATED_DIR/java/com/parmet/buf/test/v1")))
    }

    fun String.runCommand(workingDir: File): String? {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    }

    @Test
    fun `generate java`() {
        gradleRunner().withArguments("build").build()
    }

    @Test
    fun `generate java with kotlin dsl`() {
        gradleRunner().withArguments("build").build()
    }
}
