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
import org.gradle.kotlin.dsl.ivy
import org.gradle.kotlin.dsl.repositories
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

const val BUF_BINARY_CONFIGURATION_NAME = "bufTool"

internal fun Project.configureBufDependency() {
    repositories {
        ivy("https://github.com") {
            patternLayout {
                artifact("/[organization]/[module]/releases/download/v[revision]/buf-[classifier]-[ext]")
            }
            metadataSources { artifact() }
        }
    }

    val os = System.getProperty("os.name").toLowerCase()
    val (osPart, archExt) =
        when {
            os.startsWith("windows") -> "Windows" to ".exe"
            os.startsWith("linux") -> "Linux" to ""
            os.startsWith("mac") -> "Darwin" to ""
            else -> error("unsupported os: $os")
        }

    val archPart =
        when (val arch = System.getProperty("os.arch").toLowerCase()) {
            in setOf("x86_64", "amd64") -> "x86_64"
            in setOf("arm64", "aarch64") -> "arm64"
            else -> error("unsupported arch: $arch")
        }

    createConfigurationWithDependency(
        BUF_BINARY_CONFIGURATION_NAME,
        "bufbuild:buf:${getExtension().toolVersion}:$osPart@${archPart + archExt}"
    )
}

internal fun Task.execBuf(vararg args: Any, customErrorMessage: ((String) -> String)? = null) {
    execBuf(args.asList(), customErrorMessage)
}

internal fun Task.execBuf(args: Iterable<Any>, customErrorMessage: ((String) -> String)? = null) {
    if (project.hasProtobufGradlePlugin()) {
        dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
        dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
    }
    doLast {
        with(project) {
            val out = ByteArrayOutputStream()

            exec {
                val executable = singleFileFromConfiguration(BUF_BINARY_CONFIGURATION_NAME)

                if (!executable.canExecute()) {
                    executable.setExecutable(true)
                }

                workingDir(
                    if (hasProtobufGradlePlugin()) {
                        bufbuildDir
                    } else {
                        projectDir
                    }
                )

                commandLine(executable)
                setArgs(args)
                isIgnoreExitValue = true

                if (customErrorMessage != null) {
                    standardOutput = out
                }

                logger.info("Running buf from $workingDir: `buf ${args.joinToString(" ")}`")
            }.also {
                if (customErrorMessage == null) {
                    it.assertNormalExitValue()
                } else {
                    if (it.exitValue != 0) {
                        throw IllegalStateException(
                            customErrorMessage(out.toByteArray().toString(StandardCharsets.UTF_8))
                        )
                    }
                }
            }
        }
    }
}
