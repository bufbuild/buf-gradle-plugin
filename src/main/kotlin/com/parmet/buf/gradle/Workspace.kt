/*
 * Copyright (c) 2021 Andrew Parmet
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

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

const val CREATE_SYM_LINKS_TO_MODULES_TASK_NAME = "createSymLinksToModules"
const val WRITE_WORKSPACE_YAML_TASK_NAME = "writeWorkspaceYaml"

private const val EXTRACT_INCLUDE_PROTO_TASK_NAME = "extractIncludeProto"
private const val EXTRACT_PROTO_TASK_NAME = "extractProto"

internal fun Project.configureCreateSymLinksToModules() {
    tasks.register(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME) {
        workspaceCommonConfig()
        doLast { allProtoDirs().forEach { createSymLink(it) } }
    }
}

private fun Project.createSymLink(protoDir: Path) {
    val symLinkFile = File(bufbuildDir, mangle(protoDir))
    if (!symLinkFile.exists()) {
        logger.info("Creating symlink for $protoDir at $symLinkFile")
        Files.createSymbolicLink(symLinkFile.toPath(), Paths.get(bufbuildDir).relativize(file(protoDir).toPath()))
    }
}

internal fun Project.configureWriteWorkspaceYaml() {
    tasks.register(WRITE_WORKSPACE_YAML_TASK_NAME) {
        workspaceCommonConfig()

        doLast {
            val bufWork =
                """
                    |version: v1
                    |directories:
                    ${workspaceSymLinkEntries()}
                """.trimMargin()

            logger.info("Writing generated buf.work.yaml:\n$bufWork")

            File("$bufbuildDir/buf.work.yaml").writeText(bufWork)
        }
    }
}

private fun Task.workspaceCommonConfig() {
    dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
    dependsOn(EXTRACT_PROTO_TASK_NAME)
    outputs.dir(project.bufbuildDir)
    doLast { File(project.bufbuildDir).mkdirs() }
}

private fun Project.workspaceSymLinkEntries() =
    allProtoDirs().joinToString("\n") { "|  - ${mangle(it)}" }

private fun mangle(name: Path) =
    name.toString().replace("-", "--").replace(File.separator, "-")
