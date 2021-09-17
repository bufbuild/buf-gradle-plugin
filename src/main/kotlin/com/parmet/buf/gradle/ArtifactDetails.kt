package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

internal fun Project.getArtifactDetails(ext: BufExtension): ArtifactDetails? {
    val inferredDetails =
        if (ext.publishSchema) {
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

    return if (ext.publishSchema || ext.runBreakageCheck()) {
        checkNotNull(ext.imageArtifactDetails ?: inferredDetails as? ArtifactDetails) {
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
