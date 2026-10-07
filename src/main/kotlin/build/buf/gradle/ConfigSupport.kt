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

package build.buf.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.register
import java.io.File

const val COPY_BUF_CONFIG_TASK_NAME = "copyBufConfig"

internal fun Project.configureCopyBufConfig() {
    tasks.register<Copy>(COPY_BUF_CONFIG_TASK_NAME) {
        from(bufConfigFile().map { listOf(it) }.orElse(emptyList()))
        into(project.bufbuildDir)
        rename { "buf.yaml" }
    }
}

internal fun Project.bufConfigFile(): Provider<File> {
    val ext = getExtension()
    val defaultConfigFile = file("buf.yaml")
    val logger = logger
    return configurations.named(BUF_CONFIGURATION_NAME).flatMap { configuration ->
        if (configuration.dependencies.isNotEmpty()) {
            check(ext.configFileLocation == null) {
                "Buf lint configuration specified with a config file location and a dependency; pick one."
            }
            configuration.elements.map { elements ->
                val files = elements.map { it.asFile }
                val configFile =
                    checkNotNull(files.singleOrNull()) {
                        "Buf lint configuration should have exactly one file; had $files."
                    }
                logger.info("Using buf config from $configFile")
                configFile
            }
        } else {
            providers.provider {
                val configFileLocation = ext.configFileLocation
                when {
                    configFileLocation != null -> {
                        logger.info("Using buf config from $configFileLocation")
                        configFileLocation
                    }

                    defaultConfigFile.exists() -> {
                        logger.info("Using buf config from default location (project directory)")
                        defaultConfigFile
                    }

                    else -> {
                        logger.info("Using default buf config")
                        null
                    }
                }
            }
        }
    }
}
