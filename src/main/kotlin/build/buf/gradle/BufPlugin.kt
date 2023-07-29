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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.BasePlugin
import org.gradle.kotlin.dsl.apply

const val BUF_CONFIGURATION_NAME = "buf"

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            // make sure there's a `clean` and a `check`
            apply<BasePlugin>()

            createExtension()
            configurations.create(BUF_CONFIGURATION_NAME)
            configureBuf()
            withProtobufGradlePlugin { configureBufWithProtobufGradle() }
        }
    }

    private fun Project.configureBuf() {
        configureLint()
        configureFormat()
        configureBuild()
        configureGenerate()

        afterEvaluate {
            configureBufDependency()
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

    private fun Project.configureBufWithProtobufGradle() {
        failForWorkspaceAndPlugin()
        afterEvaluate {
            configureCreateSymLinksToModules()
            configureCopyBufConfig()
            configureWriteWorkspaceYaml()
        }
    }

    private fun Project.failForWorkspaceAndPlugin() {
        check(!hasWorkspace()) {
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
}
