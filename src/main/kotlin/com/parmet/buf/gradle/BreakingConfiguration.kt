

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
    registerBufTask<BreakingTask>(BUF_BREAKING_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that Protobuf API definitions are backwards-compatible with previous versions."

        dependsOn(BUF_BUILD_TASK_NAME)
    }
}
