package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.Exec

internal fun Exec.bufTask(ext: BufExtension, vararg args: Any) {
    dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
    dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
    dependsOn(COPY_BUF_CONFIG_TASK_NAME)

    commandLine("docker")
    setArgs(project.baseDockerArgs(ext) + args)
}

private fun Project.baseDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--rm",
        "--volume", "$projectDir:/workspace:Z",
        "--workdir", "/workspace/build/bufbuild",
        "bufbuild/buf:${ext.toolVersion}"
    )
