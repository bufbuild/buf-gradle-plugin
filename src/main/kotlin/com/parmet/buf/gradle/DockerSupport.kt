package com.parmet.buf.gradle

import java.io.File
import org.gradle.api.Project
import org.gradle.api.tasks.Exec

internal fun Exec.bufTask(ext: BufExtension, vararg args: Any) {
    dependsOn(COPY_PROTO_TO_WORKSPACE_TASK_NAME)
    dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)

    commandLine("docker")
    setArgs(project.baseDockerArgs(ext) + args + bufTaskConfigOption(ext))
}

private fun Exec.bufTaskConfigOption(ext: BufExtension) =
    project.resolveConfig(ext).let {
        if (it != null) {
            logger.info("Using buf config from $it")
            listOf("--config", it.readText())
        } else {
            val configFile = project.file("buf.yaml")
            if (configFile.exists()) {
                logger.info("Using buf config from default location (project directory)")
                listOf("--config", configFile.readText())
            } else {
                logger.info("Using default buf config")
                emptyList()
            }
        }
    }

private fun Project.resolveConfig(ext: BufExtension): File? =
    configurations.getByName(BUF_CONFIGURATION_NAME).let {
        if (it.dependencies.isNotEmpty()) {
            check(ext.configFileLocation == null) {
                "Buf lint configuration specified with a config file location and a dependency; pick one."
            }
            checkNotNull(it.files.singleOrNull()) {
                "Buf lint configuration should have exactly one file; had ${it.files}."
            }
        } else {
            ext.configFileLocation
        }
    }

private fun Project.baseDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--volume", "$bufbuildDir:/workspace:Z",
        "--workdir", "/workspace",
        "bufbuild/buf:${ext.toolVersion}"
    )
