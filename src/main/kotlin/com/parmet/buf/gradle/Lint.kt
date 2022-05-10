/*
 * Copyright (c) 2021 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import java.nio.file.Path

const val BUF_LINT_TASK_NAME = "bufLint"

internal fun Project.configureLint() {
    tasks.register(BUF_LINT_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that Protobuf API definitions are consistent with your chosen best practices."

        fun lintWithArgs(path: Path? = null) =
            listOfNotNull("lint", path?.let(::mangle)) +
                bufConfigFile()?.let { listOf("--config", it.readText()) }.orEmpty()

        when {
            hasProtobufGradlePlugin() ->
                srcProtoDirs().forEach { execBuf(lintWithArgs(it)) }
            hasWorkspace() ->
                execBuf("lint")
            else ->
                execBuf(lintWithArgs())
        }
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
}
