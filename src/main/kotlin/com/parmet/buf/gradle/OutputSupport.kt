package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

const val BUF_BUILD_DIR = "bufbuild"

internal val Project.bufbuildDir
    get() = "$buildDir/$BUF_BUILD_DIR"

internal fun Task.createsOutput() {
    doFirst { File(project.bufbuildDir).mkdirs() }
}

internal fun ArtifactDetails.groupAndArtifact() =
    "$groupId:$artifactId"
