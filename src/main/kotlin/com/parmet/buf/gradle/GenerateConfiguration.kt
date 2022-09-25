/*
 * Copyright (c) 2022 Andrew Parmet
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
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP

const val BUF_GENERATE_TASK_NAME = "bufGenerate"

const val GENERATED_DIR = "generated"

internal fun Project.configureGenerate() {
    if (hasGenerate()) {
        registerBufTask<GenerateTask>(BUF_GENERATE_TASK_NAME) {
            group = BUILD_GROUP
            description = "Generates code from a Protobuf schema."

            createsOutput()
        }
    }
}

private fun Project.hasGenerate() =
    file("buf.gen.yaml").let { it.exists() && it.isFile }
