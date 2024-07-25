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

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.Files.lines
import kotlin.streams.asSequence

abstract class LintTask : AbstractBufExecTask() {
    /** The input proto files. */
    @get:InputFiles
    internal abstract val inputFiles: ConfigurableFileCollection

    /** The input buf configuration file. */
    @get:InputFile
    @get:Optional
    internal abstract val bufConfigFile: Property<File>

    @TaskAction
    fun bufLint() {
        execBufInSpecificDirectory(
            "lint",
            bufConfigFile.orNull?.let { listOf("--config", it.readAndStripComments()) }.orEmpty(),
        ) {
            """
                 |Some Protobuf files had lint violations:
                 |$it
            """.trimMargin()
        }
    }

    private fun File.readAndStripComments() =
        lines(toPath()).use { lines ->
            lines.asSequence()
                .filterNot { it.matches("( ?)#.*".toRegex()) }
                .joinToString(separator = lineSeparator)
        }
}
