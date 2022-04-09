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
import org.gradle.api.tasks.Exec
import org.gradle.process.ExecSpec

internal val BUF_DOCKER_TASK_DEPENDENCIES =
    listOf(
        CREATE_SYM_LINKS_TO_MODULES_TASK_NAME,
        WRITE_WORKSPACE_YAML_TASK_NAME,
        COPY_BUF_CONFIG_TASK_NAME
    )

internal fun Exec.buf(ext: BufExtension, vararg args: Any) {
    dependsOn(BUF_DOCKER_TASK_DEPENDENCIES)

    commandLine("docker")
    val dockerArgs = project.baseDockerArgs(ext) + args
    setArgs(dockerArgs)
    project.logger.quiet("Running buf: `docker ${dockerArgs.joinToString(" ")}`")
}

internal fun ExecSpec.bufLint(project: Project, ext: BufExtension, vararg args: Any) {
    commandLine("docker")
    setArgs(project.lintDockerArgs(ext) + args)
    project.logger.quiet("Running buf: `docker ${getArgs().joinToString(" ")}`")
}

private fun Project.baseDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--rm",
        "--volume", "$projectDir:/workspace:Z",
        "--workdir", "/workspace/build/bufbuild",
        "bufbuild/buf:${ext.toolVersion}"
    )

private fun Project.lintDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--rm",
        "--volume", "$projectDir:/workspace:Z",
        "--workdir", "/workspace",
        "bufbuild/buf:${ext.toolVersion}"
    )
