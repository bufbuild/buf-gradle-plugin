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
    var configFileLocation: File? = null
    var publishSchema = false
    var previousVersion: String? = null
    var toolVersion: String = "0.48.2"

    internal var imageArtifactDetails: ArtifactDetails? = null

    fun imageArtifact(configure: Action<ArtifactDetails>) {
        imageArtifactDetails = (imageArtifactDetails ?: ArtifactDetails()).apply(configure::execute)
    }
}

class ArtifactDetails(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null
) {
    internal fun groupAndArtifact() =
        "$groupId:$artifactId"
}
