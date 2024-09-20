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

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class BreakingTask : AbstractBufExecTask() {
    @get:Input
    internal abstract val v1SyntaxOnly: Property<Boolean>

    /** The input publication file. */
    @get:InputFile
    @get:Optional
    internal abstract val publicationFile: Property<File>

    /** The input breaking config file. */
    @get:InputFile
    internal abstract val configFile: Property<File>

    @TaskAction
    fun bufBreaking() {
        val args = mutableListOf<Any>()
        args.add("breaking")
        //if (v1SyntaxOnly.get()) {
            args.add(publicationFile.get())
        //}
        args.add("--against")
        args.add(configFile.get())
        execBuf(*args.toTypedArray()) {
            """
                |Some Protobuf files had breaking changes:
                |$it
            """.trimMargin()
        }
    }
}
