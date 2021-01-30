package com.parmet.buf.gradle

import java.io.File
import org.gradle.api.Action

open class BufExtension {
    var configFileLocation: File? = null
    var publishSchema = false
    var previousVersion: String? = null
    var toolVersion: String = "0.36.0"

    internal var imageArtifactDetails: ArtifactDetails? = null

    fun imageArtifact(configure: Action<ArtifactDetails>) {
        imageArtifactDetails = ArtifactDetails().apply(configure::execute)
    }
}

class ArtifactDetails(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null
)
