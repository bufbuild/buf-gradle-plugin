package com.parmet.buf.gradle

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import org.gradle.api.Project

const val CREATE_SYM_LINKS_TO_MODULES_TASK_NAME = "createSymLinksToModules"
const val WRITE_WORKSPACE_YAML_TASK_NAME = "writeWorkspaceYaml"

private const val SRC_MAIN_PROTO = "src/main/proto"
private const val BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN = "build/extracted-include-protos/main"

internal fun Project.configureCreateSymLinksToModules() {
    tasks.register(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME) {
        dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
        outputs.dir(bufbuildDir)

        doLast {
            File(bufbuildDir).mkdirs()
            createSymLink(SRC_MAIN_PROTO)
            createSymLink(BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN)
        }
    }
}

private fun Project.createSymLink(protoDir: String) {
    val symLinkFile = File(bufbuildDir, mangle(protoDir))
    if (anyProtos(protoDir) && !symLinkFile.exists()) {
        logger.info("Creating symlink for $protoDir at $symLinkFile")
        Files.createSymbolicLink(symLinkFile.toPath(), Paths.get(bufbuildDir).relativize(file(protoDir).toPath()))
    }
}

private fun mangle(name: String) =
    name.replace("-", "--").replace(File.separator, "-")

private fun Project.anyProtos(file: String) =
    file(file).walkTopDown().any { it.extension == "proto" }

internal fun Project.configureWriteWorkspaceYaml() {
    tasks.register(WRITE_WORKSPACE_YAML_TASK_NAME) {
        outputs.dir(bufbuildDir)
        doLast {
            File(bufbuildDir).mkdirs()
            File("$bufbuildDir/buf.work.yaml").writeText(
                """
                    version: v1
                    directories:
                      ${if (anyProtos(SRC_MAIN_PROTO)) "- ${mangle(SRC_MAIN_PROTO)}" else ""}
                      ${if (anyProtos(BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN)) "- ${mangle(BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN)}" else ""}
                """.trimIndent()
            )
        }
    }
}
