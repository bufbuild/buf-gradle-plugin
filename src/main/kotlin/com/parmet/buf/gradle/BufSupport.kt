

package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import java.nio.charset.StandardCharsets

const val BUF_BINARY_CONFIGURATION_NAME = "bufTool"

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

    createConfigurationWithDependency(
        BUF_BINARY_CONFIGURATION_NAME,
        mapOf(
            "group" to "com.parmet.buf",
            "name" to "buf",
            "version" to extension.toolArtifactVersion,
            "classifier" to "${extension.toolVersion}-$osPart-$archPart",
            "ext" to "exe"
        )
    )
}

internal fun Task.execBuf(vararg args: Any, customErrorMessage: ((String) -> String)? = null) {
    execBuf(args.asList(), customErrorMessage)
}

internal fun Task.execBuf(args: Iterable<Any>, customErrorMessage: ((String) -> String)? = null) {
    with(project) {
        val executable = singleFileFromConfiguration(BUF_BINARY_CONFIGURATION_NAME)

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
