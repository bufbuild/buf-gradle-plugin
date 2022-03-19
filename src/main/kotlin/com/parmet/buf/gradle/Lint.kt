package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.register

const val BUF_LINT_TASK_NAME = "bufLint"

internal fun Project.configureLint(ext: BufExtension) {
    tasks.register<Exec>(BUF_LINT_TASK_NAME) {
        dependsOn(BUF_BUILD_TASK_NAME)
        group = CHECK_TASK_NAME
        bufTask(ext, "lint", BUF_BUILD_PUBLICATION_FILENAME)
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
}
