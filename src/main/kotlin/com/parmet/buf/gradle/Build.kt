package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Exec
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the

const val BUF_BUILD_TASK_NAME = "bufBuild"
const val BUF_BUILD_PUBLICATION_FILENAME = "image.bin"
const val BUF_IMAGE_PUBLICATION_NAME = "bufImagePublication"

internal fun Project.configureBuild(ext: BufExtension, artifactDetails: ArtifactDetails) {
    logger.info("Publishing buf schema image to ${artifactDetails.groupAndArtifact()}:${artifactDetails.version}")

    tasks.register<Exec>(BUF_BUILD_TASK_NAME) {
        bufTask(ext, "build", "--output", BUF_BUILD_PUBLICATION_FILENAME)
    }

    the<PublishingExtension>().publications {
        create<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
            groupId = artifactDetails.groupId
            artifactId = artifactDetails.artifactId
            version = artifactDetails.version

            artifact(file("$bufbuildDir/$BUF_BUILD_PUBLICATION_FILENAME")) {
                builtBy(tasks.named(BUF_BUILD_TASK_NAME))
            }
        }
    }
}
