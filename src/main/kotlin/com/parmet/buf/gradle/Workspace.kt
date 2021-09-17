package com.parmet.buf.gradle

import java.io.File
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register

const val WRITE_WORKSPACE_YAML_TASK_NAME = "writeWorkspaceYaml"
const val COPY_PROTO_TO_WORKSPACE_TASK_NAME = "copyProtoToWorkspace"
const val WORKSPACE_DIR = "workspace"

internal fun Project.configureWriteWorkspaceYaml() {
    tasks.register(WRITE_WORKSPACE_YAML_TASK_NAME) {
        outputs.dir(bufbuildDir)
        doLast {
            File(bufbuildDir).mkdirs()
            File("$bufbuildDir/buf.work.yaml").writeText(
                """
                    version: v1
                    directories:
                      - workspace
                """.trimIndent()
            )
        }
    }
}

internal fun Project.configureCopyProtoToWorkspace() {
    tasks.register<Copy>(COPY_PROTO_TO_WORKSPACE_TASK_NAME) {
        dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)

        from("src/main/proto", "build/extracted-include-protos/main")
        into("${project.bufbuildDir}/$WORKSPACE_DIR")
    }
}
