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
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateTask : AbstractBufExecTask() {
    /** Whether to include imports. */
    @get:Input
    internal abstract val includeImports: Property<Boolean>

    /** Template file. */
    @get:InputFile
    @get:Optional
    internal abstract val templateFile: Property<File>

    /** The input proto files. */
    @get:InputFiles
    internal abstract val inputFiles: ConfigurableFileCollection

    /** The directory to output generated files. */
    @get:OutputDirectory
    internal abstract val outputDirectory: Property<File>

    @TaskAction
    fun bufGenerate() {
        val args = listOf("generate", "--output", outputDirectory.get())
        execBuf(args + additionalArgs())
    }

    private fun additionalArgs(): List<String> {
        val importOptions =
            if (includeImports.get()) {
                listOf("--include-imports")
            } else {
                emptyList()
            }

        val templateFileOption =
            templateFile.orNull?.let {
                listOf("--template", it.absolutePath)
            } ?: emptyList()

        return importOptions + templateFileOption
    }
}
