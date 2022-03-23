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
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME

const val BUF_BREAKING_EXTRACT_TASK_NAME = "bufBreakingExtract"
const val BUF_BREAKING_TASK_NAME = "bufBreaking"
const val BUF_BREAKING_CONFIGURATION_NAME = "bufBreaking"
const val BREAKING_DIR = "breaking"

internal fun Project.configureBreaking(ext: BufExtension, artifactDetails: ArtifactDetails) {
    addSchemaDependency(ext, artifactDetails)

    val bufBreakingFile = LazyBufBreakingFile()
    configureSchemaExtraction(bufBreakingFile)
    configureBreakingTask(ext, bufBreakingFile)

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_BREAKING_TASK_NAME)
}

private fun Project.addSchemaDependency(ext: BufExtension, artifactDetails: ArtifactDetails) {
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

    configurations.create(BUF_BREAKING_CONFIGURATION_NAME)

    dependencies {
        add(BUF_BREAKING_CONFIGURATION_NAME, "${artifactDetails.groupAndArtifact()}:$versionSpecifier")
    }
}

private fun Project.configureSchemaExtraction(bufBreakingFile: LazyBufBreakingFile) {
    tasks.register<Copy>(BUF_BREAKING_EXTRACT_TASK_NAME) {
        outputs.upToDateWhen { false }
        val breakingDir = file("$bufbuildDir/$BREAKING_DIR")

        doFirst { breakingDir.deleteRecursively() }

        from(configurations.getByName(BUF_BREAKING_CONFIGURATION_NAME).files)
        into(breakingDir)

        doLast {
            val copiedFiles = breakingDir.listFiles().orEmpty()

            val fileName =
                checkNotNull(copiedFiles.singleOrNull()) {
                    "Unable to resolve a single file from Buf schema publication. Found $copiedFiles. Please " +
                        "file an issue at https://github.com/andrewparmet/buf-gradle-plugin/issues/new if you " +
                        "see this error."
                }.name

            logger.info("Buf will check schema dependency against $fileName")

            bufBreakingFile.fileName = fileName
        }
    }
}

private fun Project.configureBreakingTask(ext: BufExtension, bufBreakingFile: LazyBufBreakingFile) {
    tasks.register<Exec>(BUF_BREAKING_TASK_NAME) {
        dependsOn(BUF_BREAKING_EXTRACT_TASK_NAME)
        dependsOn(BUF_BUILD_TASK_NAME)

        group = CHECK_TASK_NAME

        bufTask(
            ext,
            "breaking",
            BUF_BUILD_PUBLICATION_FILENAME,
            "--against",
            bufBreakingFile
        )
    }
}

private class LazyBufBreakingFile(var fileName: String? = null) {
    override fun toString() =
        "$BREAKING_DIR/$fileName"
}
