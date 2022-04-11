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

internal fun Task.execBuf(vararg args: Any) {
    execBuf(args.asList())
}

internal fun Task.execBuf(args: Iterable<Any>) {
    if (project.hasProtobufGradlePlugin()) {
        dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
        dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
    }
    doLast {
        project.exec {
            // todo: working dir can be set based on hasProtobufGradlePlugin() and then file locations can be absolute
            
            commandLine("docker")
            val dockerArgs = project.baseDockerArgs() + args
            setArgs(dockerArgs)
            logger.info("Running buf: `docker ${dockerArgs.joinToString(" ")}`")
        }
    }
}

private fun Project.baseDockerArgs() =
    listOf(
        "run",
        "--rm",
        "--volume", "$projectDir:/workspace:Z",
        "--workdir", bufWorkingDir(),
        "bufbuild/buf:${getExtension().toolVersion}"
    )

private fun Project.bufWorkingDir() =
    "/workspace" +
        if (hasProtobufGradlePlugin()) {
            "/${buildDir.name}/$BUF_BUILD_DIR"
        } else {
            ""
        }

internal fun Project.qualifyFile(name: String) =
    qualifyFile { name }

internal fun Project.qualifyFile(name: () -> String) =
    object : Any() {
        override fun toString() =
            if (hasProtobufGradlePlugin()) {
                ""
            } else {
                "${buildDir.name}/$BUF_BUILD_DIR/"
            } + name()
    }
