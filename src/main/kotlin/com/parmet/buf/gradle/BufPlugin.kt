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

import org.gradle.api.Plugin
import org.gradle.api.Project

const val BUF_CONFIGURATION_NAME = "buf"

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.createExtension()
        project.configurations.create(BUF_CONFIGURATION_NAME)

        if (project.hasProtobufGradlePlugin()) {
            project.failForWorkspaceAndPlugin()
            project.afterEvaluate { configureBufWithProtobufGradle() }
        } else {
            project.withProtobufGradlePlugin { project.failForWorkspaceAndPlugin() }
            project.configureBuf()
        }
    }

    private fun Project.failForWorkspaceAndPlugin() {
        check(!project.hasWorkspace()) {
            """
                A project cannot use both the protobuf-gradle-plugin and a Buf workspace.
                If you have multiple protobuf source directories and you would like to
                use the protobuf-gradle-plugin, configure the protobuf-gradle-plugin to use
                those directories as source directories in the appropriate source set. If you
                would like to use a Buf workspace, you must configure dependency resolution and
                code generation using Buf. There is no (easy) way to reconcile the two
                configurations for linting, breakage, and code generation steps.
            """.trimIndent().replace('\n', ' ')
        }
    }

    private fun Project.configureBufWithProtobufGradle() {
        configureCreateSymLinksToModules()
        configureCopyBufConfig()
        configureWriteWorkspaceYaml()
        configureBuf()
    }

    private fun Project.configureBuf() {
        configureBufDependency()
        configureLint()
        configureBuild()
        configureGenerate()

        afterEvaluate {
            getArtifactDetails()?.let {
                if (publishSchema()) {
                    configureImagePublication(it)
                }
                if (runBreakageCheck()) {
                    configureBreaking(it)
                }
            }
        }
    }
}
