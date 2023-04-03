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
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

internal fun Project.getArtifactDetails(): ArtifactDetails? {
    val inferredDetails =
        if (publishSchema()) {
            val publications = the<PublishingExtension>().publications.withType<MavenPublication>()
            publications.singleOrNull()?.let {
                ArtifactDetails(
                    it.groupId,
                    "${it.artifactId}-bufbuild",
                    it.version
                )
            } ?: publications.size
        } else {
            null
        }

    return if (publishSchema() || runBreakageCheck()) {
        checkNotNull(getExtension().imageArtifactDetails ?: inferredDetails as? ArtifactDetails) {
            """
                Unable to determine image artifact details and schema publication or
                compatibility check was requested; no image publication details
                were provided and there was not exactly one publication from which
                to infer them (found ${inferredDetails ?: 0}). Either configure the
                plugin with imageArtifact() or configure a publication.
            """.trimIndent().replace('\n', ' ')
        }
    } else {
        null
    }
}
