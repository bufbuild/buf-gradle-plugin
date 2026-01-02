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
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.annotations.VisibleForTesting
import java.nio.charset.StandardCharsets

const val BUF_BINARY_CONFIGURATION_NAME = "bufTool"

internal fun Project.createBufBinaryDependencyConfiguration() {
    configurations.create(BUF_BINARY_CONFIGURATION_NAME)
}

internal fun Project.configureBufDependency() {
    val os = System.getProperty("os.name").lowercase()
    val osPart =
        when {
            os.startsWith("windows") -> "windows"
            os.startsWith("linux") -> "linux"
            os.startsWith("mac") -> "osx"
            else -> error("unsupported os: $os")
        }

    val archPart =
        when (val arch = System.getProperty("os.arch").lowercase()) {
            in setOf("x86_64", "amd64") -> "x86_64"
            in setOf("arm64", "aarch64") -> "aarch_64"
            else -> error("unsupported arch: $arch")
        }

    val extension = getExtension()

    dependencies {
        add(
            BUF_BINARY_CONFIGURATION_NAME,
            "build.buf:buf:${extension.toolVersion}:$osPart-$archPart@exe",
        )
    }
}

internal fun AbstractBufExecTask.execBuf(
    vararg args: Any,
    customErrorMessage: ((String) -> String)? = null,
) {
    execBuf(args.asList(), customErrorMessage)
}

internal fun AbstractBufExecTask.execBuf(
    args: Iterable<Any>,
    customErrorMessage: ((String) -> String)? = null,
) {
    val executable = bufExecutable.singleFile

    if (!executable.canExecute()) {
        executable.setExecutable(true)
    }

    val processArgs = listOf(executable.absolutePath) + args
    val workingDirValue = workingDir.get()

    logger.info("Running buf from $workingDirValue: `buf ${args.joinToString(" ")}`")
    val result = ProcessRunner().use { it.shell(workingDirValue, processArgs) }

    handleResult(result, customErrorMessage)
}

@VisibleForTesting
internal fun handleResult(
    result: ProcessRunner.Result,
    customErrorMessage: ((String) -> String)?,
) {
    if (result.exitCode != 0) {
        if (customErrorMessage != null) {
            val stdOut = result.stdOut.toString(StandardCharsets.UTF_8)
            val stdErr = result.stdErr.toString(StandardCharsets.UTF_8)
            if (stdOut.isEmpty()) {
                error(result.toString())
            } else {
                val ex = IllegalStateException(customErrorMessage(stdOut))
                if (stdErr.isNotEmpty()) {
                    ex.addSuppressed(IllegalStateException(result.toString()))
                }
                throw ex
            }
        } else {
            error(result.toString())
        }
    }
}

internal fun AbstractBufExecTask.obtainDefaultProtoFileSet() =
    project.fileTree(workingDir.get()) {
        include("**/*.proto")
        // not to interfere with random plugins producing output to build dir
        exclude("build")
    }
