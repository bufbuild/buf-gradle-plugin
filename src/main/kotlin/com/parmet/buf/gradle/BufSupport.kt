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

package com.parmet.buf.gradle

import build.buf.gradle.downloadBufCLI
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.nio.charset.StandardCharsets

internal fun Project.configureBufDependency() {
    val os = System.getProperty("os.name").toLowerCase()
    val osPart =
        when {
            os.startsWith("windows") -> "windows"
            os.startsWith("linux") -> "linux"
            os.startsWith("mac") -> "osx"
            else -> error("unsupported os: $os")
        }

    val archPart =
        when (val arch = System.getProperty("os.arch").toLowerCase()) {
            in setOf("x86_64", "amd64") -> "x86_64"
            in setOf("arm64", "aarch64") -> "arm64"
            else -> error("unsupported arch: $arch")
        }
    val extension = getExtension()
    downloadBufCLI("https://github.com/bufbuild/buf/releases/download/v${extension.toolArtifactVersion}/${extension.toolVersion}-$osPart-$archPart.tar.gz", buildDir)
}

internal fun Task.execBuf(vararg args: Any, customErrorMessage: ((String) -> String)? = null) {
    execBuf(args.asList(), customErrorMessage)
}

internal fun Task.execBuf(args: Iterable<Any>, customErrorMessage: ((String) -> String)? = null) {
    with(project) {
        val executable = File(buildDir, "buf")

        if (!executable.canExecute()) {
            executable.setExecutable(true)
        }

        val workingDir =
            if (hasProtobufGradlePlugin()) {
                bufbuildDir
            } else {
                projectDir
            }

        val processArgs = listOf(executable.absolutePath) + args

        logger.info("Running buf from $workingDir: `buf ${args.joinToString(" ")}`")
        val result = ProcessRunner().use { it.shell(workingDir, processArgs) }

        if (result.exitCode != 0) {
            if (customErrorMessage != null) {
                val stdOut = result.stdOut.toString(StandardCharsets.UTF_8)
                val stdErr = result.stdErr.toString(StandardCharsets.UTF_8)
                val ex = IllegalStateException(customErrorMessage(stdOut))
                if (stdErr.isNotEmpty()) {
                    ex.addSuppressed(IllegalStateException(result.toString()))
                }
                throw ex
            } else {
                error(result.toString())
            }
        }
    }
}
