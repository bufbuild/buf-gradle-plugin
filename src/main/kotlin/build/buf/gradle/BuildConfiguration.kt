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
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.the
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP
import java.io.File

const val BUF_BUILD_TASK_NAME = "bufBuild"
const val BUF_BUILD_PUBLICATION_FILE_BASE_NAME = "image"
const val BUF_IMAGE_PUBLICATION_NAME = "bufImagePublication"

internal fun Project.configureBuild() {
    registerBufTask<BuildTask>(BUF_BUILD_TASK_NAME) {
        group = BUILD_GROUP
        description = "Builds a Buf image from a Protobuf schema."

        if (hasProtobufGradlePlugin()) {
            dependsOn(COPY_BUF_CONFIG_TASK_NAME)
        } else {
            // Called already during workspace configuration if the protobuf-gradle-plugin has been applied
            createsOutput()
        }
    }
}

internal fun Project.configureImagePublication(artifactDetails: ArtifactDetails) {
    logger.info("Publishing buf schema image to ${artifactDetails.groupAndArtifact()}:${artifactDetails.version}")

    val specifiedExtension = getExtension().bufBuildPublicationFileExtension.extension

    the<PublishingExtension>().publications {
        create<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
            groupId = artifactDetails.groupId
            artifactId = artifactDetails.artifactId
            version = artifactDetails.version

            artifact(bufBuildPublicationFile) {
                builtBy(tasks.named(BUF_BUILD_TASK_NAME))
                extension = specifiedExtension
            }
        }
    }
}

internal val Project.bufBuildPublicationFile
    get() = File(bufbuildDir, "$BUF_BUILD_PUBLICATION_FILE_BASE_NAME.${getExtension().bufBuildPublicationFileExtension.extension}")

internal val Task.bufBuildPublicationFile
    get() = project.bufBuildPublicationFile
