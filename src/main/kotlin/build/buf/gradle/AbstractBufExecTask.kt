// Copyright 2024 Buf Technologies, Inc.
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
import org.gradle.api.tasks.InputFiles
import java.io.File

/**
 * A task executing buf executable as part of its operation.
 */
abstract class AbstractBufExecTask : AbstractBufTask() {
    /** The buf executable. */
    @get:InputFiles
    internal abstract val bufExecutable: ConfigurableFileCollection

    /**
     * The directory in which buf is executed.
     * The actual real input files in this directory have to be tracked per command separately,
     * so it is just an @Input, not @InputDirectory.
     */
    @get:Input
    internal abstract val workingDir: Property<File>

    /** Whether the project has protobuf plugin enabled. */
    @get:Input
    internal abstract val hasProtobufGradlePlugin: Property<Boolean>

    /** Directories possibly containing input .proto files. */
    @get:InputFiles
    internal abstract val candidateProtoDirs: ConfigurableFileCollection

    /** Whether the project has buf workspace or not. */
    @get:Input
    internal abstract val hasWorkspace: Property<Boolean>
}
