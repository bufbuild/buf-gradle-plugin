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

import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import java.io.File

const val BUF_GENERATE_TASK_NAME = "bufGenerate"

const val GENERATED_DIR = "generated"

internal fun Project.configureGenerate() {
    registerBufExecTask<GenerateTask>(BUF_GENERATE_TASK_NAME) {
        group = BUILD_GROUP
        description = "Generates code from a Protobuf schema."

        val generateOptions = project.getExtension().generateOptions
        includeImports.set(generateOptions?.includeImports ?: false)
        templateFile.set(generateOptions?.let { resolveTemplateFile(it) })
        inputFiles.setFrom(obtainDefaultProtoFileSet())
        outputDirectory.set(File(project.bufbuildDir, GENERATED_DIR))
    }
}

private fun Project.resolveTemplateFile(generateOptions: GenerateOptions): File {
    val defaultTemplateFile = project.file("buf.gen.yaml").validOrNull()
    return if (generateOptions.templateFileLocation != null) {
        val specifiedTemplateFile = generateOptions.templateFileLocation.validOrNull()
        check(specifiedTemplateFile != null) {
            "Specified templateFileLocation does not exist."
        }
        check(defaultTemplateFile == null || specifiedTemplateFile == defaultTemplateFile) {
            "Buf gen template file specified in the project directory as well as with templateFileLocation; pick one."
        }
        specifiedTemplateFile
    } else {
        check(defaultTemplateFile != null) {
            "No buf.gen.yaml file found in the project directory."
        }
        defaultTemplateFile
    }
}

private fun File?.validOrNull() = this?.takeIf { it.isFile && it.exists() }
