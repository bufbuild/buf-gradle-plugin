// Copyright 2023 Buf Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.



package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register
import java.io.File

const val COPY_BUF_CONFIG_TASK_NAME = "copyBufConfig"

internal fun Project.configureCopyBufConfig() {
    tasks.register<Copy>(COPY_BUF_CONFIG_TASK_NAME) {
        from(listOfNotNull(bufConfigFile()))
        into(bufbuildDir)
        rename { "buf.yaml" }
    }
}

internal fun Project.bufConfigFile() =
    project.resolveConfig().let {
        if (it != null) {
            logger.info("Using buf config from $it")
            it
        } else {
            val configFile = project.file("buf.yaml")
            if (configFile.exists()) {
                logger.info("Using buf config from default location (project directory)")
                configFile
            } else {
                logger.info("Using default buf config")
                null
            }
        }
    }

internal fun Task.bufConfigFile() =
    project.bufConfigFile()

private fun Project.resolveConfig(): File? {
    val ext = getExtension()
    return configurations.getByName(BUF_CONFIGURATION_NAME).let {
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
}
