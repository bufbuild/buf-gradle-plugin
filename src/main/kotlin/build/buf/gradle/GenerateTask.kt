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
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault
import java.io.File

@DisableCachingByDefault(because = "executes buf CLI which may use remote plugins")
abstract class GenerateTask : AbstractBufExecTask() {
    /** Whether to include imports. */
    @get:Input
    internal abstract val includeImports: Property<Boolean>

    /** Template file. */
    @get:InputFile
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val templateFile: Property<File>

    /** The input proto files. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val inputFiles: ConfigurableFileCollection

    /** The input passed to buf generate. */
    @get:Input
    @get:Optional
    internal abstract val input: Property<String>

    /** The protoc executable made available to protoc_builtin plugins. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val protocExecutable: ConfigurableFileCollection

    /** Directories added to the front of the PATH. */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    internal abstract val pluginPath: ConfigurableFileCollection

    /** The directory to output generated files. */
    @get:OutputDirectory
    internal abstract val outputDirectory: Property<File>

    @TaskAction
    fun bufGenerate() {
        val args = listOf("generate", "--output", outputDirectory.get())
        execBuf(args + additionalArgs(), environment = pathEnvironment())
    }

    private fun pathEnvironment(): Map<String, String> {
        val pathEntries =
            buildList {
                if (!protocExecutable.isEmpty) {
                    add(stageProtoc())
                }
                addAll(pluginPath.files)
            }
        if (pathEntries.isEmpty()) {
            return emptyMap()
        }
        val path = pathEntries.map(File::getAbsolutePath) + listOfNotNull(System.getenv("PATH"))
        return mapOf("PATH" to path.joinToString(File.pathSeparator))
    }

    /** Copies protoc into a directory under the name buf looks up on the PATH. */
    private fun stageProtoc(): File {
        val binDir = File(temporaryDir, "bin")
        val isWindows = System.getProperty("os.name").lowercase().startsWith("windows")
        val protoc = File(binDir, if (isWindows) "protoc.exe" else "protoc")
        protocExecutable.singleFile.copyTo(protoc, overwrite = true)
        protoc.setExecutable(true)
        return binDir
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

        return importOptions + templateFileOption + listOfNotNull(input.orNull)
    }
}
