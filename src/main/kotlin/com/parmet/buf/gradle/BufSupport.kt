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
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.ivy
import org.gradle.kotlin.dsl.repositories
import kotlin.reflect.KFunction

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

    val osPart =
        when {
            os.startsWith("windows") -> "Windows"
            os.startsWith("linux") -> "Linux"
            os.startsWith("mac") -> "Darwin"
            else -> error("unsupported os: $os")
        }

    val arch = System.getProperty("os.arch").toLowerCase()
    require(arch in setOf("x86_64", "aarch64", "arm64")) { "unsupported arch: $arch" }

    val version = getExtension().toolVersion

    configurations.create(BUF_BINARY_CONFIGURATION_NAME)

    dependencies {
        add(BUF_BINARY_CONFIGURATION_NAME, "bufbuild:buf:$version:$osPart@$arch")
    }
}

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
            project.configurations.getByName(BUF_BINARY_CONFIGURATION_NAME).singleFile.setExecutable(true)

            workingDir(
                if (project.hasProtobufGradlePlugin()) {
                    project.bufbuildDir
                } else {
                    project.projectDir
                }
            )

            commandLine(project.configurations.getByName(BUF_BINARY_CONFIGURATION_NAME).singleFile.path)
            setArgs(args)

            logger.info("Running buf from $workingDir: `buf ${args.joinToString(" ")}`")
        }
    }
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
