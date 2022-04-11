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
import org.gradle.api.plugins.JavaBasePlugin.CHECK_TASK_NAME
import java.nio.file.Path

const val BUF_LINT_TASK_NAME = "bufLint"

internal fun Project.configureLint() {
    tasks.register(BUF_LINT_TASK_NAME) {
        group = CHECK_TASK_NAME

        if (hasProtobufGradlePlugin()) {
            dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
            dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
        }

        doLast {
            fun lintWithArgs(path: Path? = null) =
                listOfNotNull("lint", path?.let(::mangle)) +
                    bufConfigFile()?.let { listOf("--config", it.readText()) }.orEmpty()

            when {
                hasProtobufGradlePlugin() ->
                    srcProtoDirs().forEach { exec { buf(this@configureLint, lintWithArgs(it)) } }
                hasWorkspace() ->
                    exec { buf(this@configureLint, listOf("lint")) }
                else ->
                    exec { buf(this@configureLint, lintWithArgs()) }
            }
        }
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
}
