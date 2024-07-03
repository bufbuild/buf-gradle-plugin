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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

abstract class FormatApplyTask : AbstractBufExecTask() {
    /** The input files to be formatted. */
    @get:InputFiles
    internal abstract val inputFiles: ConfigurableFileCollection

    /** The output files that have been formatted. */
    @get:OutputFiles
    internal abstract val outputFiles: ConfigurableFileCollection

    @TaskAction
    fun bufFormatApply() {
        execBufInSpecificDirectory("format", "-w")
    }
}
