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
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create

const val BUF_CONFIGURATION_NAME = "buf"

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<BufExtension>("buf")
        project.configurations.create(BUF_CONFIGURATION_NAME)

        project.afterEvaluate {
            project.configureCreateSymLinksToModules()
            project.configureCopyBufConfig(ext)
            project.configureWriteWorkspaceYaml()
            project.configureLint(ext)
            project.configureBuild(ext)
            project.getArtifactDetails(ext)?.let {
                if (ext.publishSchema) {
                    project.configureImagePublication(it)
                }
                if (ext.runBreakageCheck()) {
                    project.configureBreaking(ext, it)
                }
            }
        }
    }
}

internal fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

internal val Project.bufbuildDir
    get() = "$buildDir/bufbuild"

internal fun BufExtension.runBreakageCheck() =
    checkSchemaAgainstLatestRelease || previousVersion != null

internal fun ArtifactDetails.groupAndArtifact() =
    "$groupId:$artifactId"
