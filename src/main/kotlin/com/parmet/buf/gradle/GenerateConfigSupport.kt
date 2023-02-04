package com.parmet.buf.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

const val CHECK_BUF_GEN_CONFIG_TASK_NAME = "checkBufGenConfig"
internal fun Project.configureCheckBufGenTask() {
    tasks.register<DefaultTask>(CHECK_BUF_GEN_CONFIG_TASK_NAME) {
        project.checkGenConfig()
    }

    tasks.named(BUF_GENERATE_TASK_NAME).dependsOn(CHECK_BUF_GEN_CONFIG_TASK_NAME)
}

private fun Project.checkGenConfig() {
    val ext = getExtension()
    ext.generateOptions?.let { generateOptions ->
        val selectedGenFiles = listOfNotNull(generateOptions.genFileLocation, file("buf.gen.yaml")).filter {
            it.exists() && it.isFile
        }
        check(selectedGenFiles.isNotEmpty()) {
            "No buf.gen.yaml file found in the root directory or with genFileLocation."
        }
        check(selectedGenFiles.size == 1) {
            "Buf gen configuration file specified in the root directory as well as with genFileLocation; pick one."
        }
    }
}
