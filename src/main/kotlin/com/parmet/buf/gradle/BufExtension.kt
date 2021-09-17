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

import java.io.File
import org.gradle.api.Action

open class BufExtension {
    /**
     * Specify the location of buf.yaml.
     */
    var configFileLocation: File? = null

    /**
     * Publish an image.json artifact with the complete descriptor set.
     */
    var publishSchema = false

    /**
     * If set, enables breakage checking against the image published with the provided version. In general, prefer
     * checking against the latest release with [checkSchemaAgainstLatestRelease].
     */
    var previousVersion: String? = null

    /**
     * If set, checks the project's schema against the latest published schema. To check against a constant version use
     * [previousVersion].
     */
    var checkSchemaAgainstLatestRelease = false

    /**
     * Specify the version of Buf.
     */
    var toolVersion: String = "0.54.1"

    internal var imageArtifactDetails: ArtifactDetails? = null

    /**
     * Specify the artifact details for schema publication.
     */
    fun imageArtifact(configure: Action<ArtifactDetails>) {
        imageArtifactDetails = (imageArtifactDetails ?: ArtifactDetails()).apply(configure::execute)
    }
}

class ArtifactDetails(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null
)
