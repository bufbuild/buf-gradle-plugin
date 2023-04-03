

package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

const val BUF_LINT_TASK_NAME = "bufLint"

internal fun Project.configureLint() {
    registerBufTask<LintTask>(BUF_LINT_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that a Protobuf schema conforms to the Buf lint configuration."
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
}
