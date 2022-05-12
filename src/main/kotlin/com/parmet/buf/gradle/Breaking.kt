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

const val BUF_BREAKING_TASK_NAME = "bufBreaking"
const val BUF_BREAKING_CONFIGURATION_NAME = "bufBreaking"

internal fun Project.configureBreaking(artifactDetails: ArtifactDetails) {
    addSchemaDependency(artifactDetails)
    configureBreakingTask()

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_BREAKING_TASK_NAME)
}

private fun Project.addSchemaDependency(artifactDetails: ArtifactDetails) {
    val ext = getExtension()

    val versionSpecifier =
        if (ext.checkSchemaAgainstLatestRelease) {
            require(ext.previousVersion == null) {
                "Cannot configure bufBreaking against latest release and a previous version."
            }
            "latest.release"
        } else {
            ext.previousVersion
        }

    logger.info("Resolving buf schema image from ${artifactDetails.groupAndArtifact()}:$versionSpecifier")

    createConfigurationWithDependency(
        BUF_BREAKING_CONFIGURATION_NAME,
        "${artifactDetails.groupAndArtifact()}:$versionSpecifier"
    )
}

private fun Project.configureBreakingTask() {
    tasks.register(BUF_BREAKING_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that Protobuf API definitions are backwards-compatible with previous versions."

        dependsOn(BUF_BUILD_TASK_NAME)

        execBuf(
            "breaking",
            bufBuildPublicationFile,
            "--against",
            singleFileFromConfiguration(BUF_BREAKING_CONFIGURATION_NAME)
        ) {
            """
                |Some Protobuf files had breaking changes:
                |$it
            """.trimMargin()
        }
    }
}
