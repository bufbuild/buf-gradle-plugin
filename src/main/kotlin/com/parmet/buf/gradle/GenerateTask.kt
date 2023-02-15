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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class GenerateTask : DefaultTask() {
    @TaskAction
    fun bufGenerate() {
        val args = listOf("generate", "--output", File(bufbuildDir, GENERATED_DIR))
        execBuf(args + additionalArgs())
    }

    private fun additionalArgs(): List<String> {
        val generateOptions = getExtension().generateOptions
        val importOptions = if (generateOptions?.includeImports == true) {
            listOf("--include-imports")
        } else {
            emptyList()
        }

        val configOptions = genConfigFile()?.let {
            listOf("--template", it.absolutePath)
        } ?: emptyList()

        return importOptions + configOptions
    }

    private fun genConfigFile(): File? {
        return getExtension().generateOptions?.let { generateOptions ->
            val selectedTemplateFiles = listOfNotNull(generateOptions.templateFileLocation, project.file("buf.gen.yaml")).filter {
                it.exists() && it.isFile
            }
            check(selectedTemplateFiles.isNotEmpty()) {
                "No buf.gen.yaml file found in the root directory or with templateFileLocation."
            }
            check(selectedTemplateFiles.size == 1) {
                "Buf gen template file specified in the root directory as well as with templateFileLocation; pick one."
            }

            selectedTemplateFiles.single()
        }
    }
}
