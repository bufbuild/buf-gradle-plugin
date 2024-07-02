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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class BuildTask : AbstractBufExecTask() {
    /** The input files. */
    @get:InputFiles
    internal abstract val inputFiles: ConfigurableFileCollection

    /** The output publication file. */
    @get:OutputFile
    internal abstract val publicationFile: Property<File>

    @TaskAction
    fun bufBuild() {
        execBuf("build", "--output", publicationFile.get())
    }
}
