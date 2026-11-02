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
import java.io.File

class GenerateTest : AbstractGenerateTest() {
    @Test
    fun `generate java with protoc version`() {
        // Shadows any protoc installed on the machine, so the build only passes
        // if bufGenerate puts the resolved protoc ahead of it on the PATH.
        val failingProtocDir = File(projectDir, "failing-protoc")
        File(failingProtocDir, "protoc").setExecutable(true)

        gradleRunner().withEnvironment(environmentWithPathPrefix(failingProtocDir)).withArguments("build").build()
    }

    @Test
    fun `generate java with input`() {
        gradleRunner().withArguments(BUF_GENERATE_TASK_NAME).build()

        assertThat(File(projectDir, "build/bufbuild/generated/java/buf/test/v1/Test.java")).exists()
    }

    @Test
    fun `generate with plugin path`() {
        File(projectDir, "plugins/protoc-gen-marker").setExecutable(true)

        gradleRunner().withArguments(BUF_GENERATE_TASK_NAME).build()

        assertThat(File(projectDir, "plugin-ran")).exists()
    }

    private fun environmentWithPathPrefix(directory: File): Map<String, String> {
        val environment = System.getenv().toMutableMap()
        // Windows spells it "Path", and the copied map is case-sensitive.
        val pathKey = environment.keys.first { it.equals("PATH", ignoreCase = true) }
        environment[pathKey] = directory.absolutePath + File.pathSeparator + environment.getValue(pathKey)
        return environment
    }
}
