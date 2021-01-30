package com.parmet.buf.gradle

import com.parmet.buf.gradle.BufPlugin.Companion.BUF_CONFIGURATION_NAME
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<BufExtension>("buf")
        project.configurations.create(BUF_CONFIGURATION_NAME)
        project.configureLint(ext)

        val artifactDetails = project.getArtifactDetails(ext)
        project.configureBuild(ext, artifactDetails)
        project.configureBreaking(ext, artifactDetails)
    }

    private fun Project.configureLint(ext: BufExtension) {
        tasks.register<Exec>(BUF_LINT_TASK_NAME) {
            group = JavaBasePlugin.CHECK_TASK_NAME
            bufTask(ext, "lint")
        }

        afterEvaluate {
            tasks.named(BUF_LINT_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
            tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
        }
    }

    /**
     * Returns a box that will contain the artifact details for a built image if
     * the plugin configuration needs them.
     */
    private fun Project.getArtifactDetails(ext: BufExtension): AtomicReference<ArtifactDetails> {
        val box = AtomicReference<ArtifactDetails>()

        afterEvaluate {
            if (ext.publishSchema || ext.previousVersion != null) {
                the<PublishingExtension>().publications {
                    val publications = withType<MavenPublication>()
                    val artifactDetails = ext.imageArtifactDetails
                        ?: publications.singleOrNull()?.let {
                            ArtifactDetails(
                                it.groupId,
                                "${it.artifactId}-$BUF_IMAGE_PUBLICATION_NAME",
                                it.version
                            )
                        } ?: error(
                            "Unable to determine image artifact details and schema publication or" +
                                "schema compatibility check was requested; no image publication " +
                                "details provided and existing publications did not have exactly " +
                                "one element to infer them. Had size ${publications.size}. Either " +
                                "configure the plugin with imagePublicationDetails() or configure " +
                                "a publication."
                        )

                    box.set(artifactDetails)
                }
            }
        }

        return box
    }

    private fun Project.configureBuild(ext: BufExtension, artifactDetails: AtomicReference<ArtifactDetails>) {
        val bufBuildImage = "$BUF_BUILD_DIR/image.json"

        tasks.register<Exec>(BUF_BUILD_TASK_NAME) {
            doFirst {
                file("$buildDir/$BUF_BUILD_DIR").mkdirs()
            }
            bufTask(
                ext,
                "build",
                "--output",
                "$relativeBuildDir/$bufBuildImage"
            )
        }

        afterEvaluate {
            tasks.named(BUF_BUILD_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
        }

        afterEvaluate {
            if (ext.publishSchema) {
                the<PublishingExtension>().publications {
                    create<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
                        groupId = artifactDetails.get()?.groupId
                        artifactId = artifactDetails.get()?.artifactId
                        version = artifactDetails.get()?.version

                        artifact(file("$buildDir/$bufBuildImage")) {
                            builtBy(tasks.named(BUF_BUILD_TASK_NAME))
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureBreaking(ext: BufExtension, artifactDetails: AtomicReference<ArtifactDetails>) {
        val bufbuildBreaking = "$BUF_BUILD_DIR/breaking"

        configurations.create(BUF_BREAKING_CONFIGURATION_NAME)
        dependencies {
            afterEvaluate {
                if (ext.previousVersion != null) {
                    add(
                        BUF_BREAKING_CONFIGURATION_NAME,
                        "${artifactDetails.get()?.groupId}:${artifactDetails.get()?.artifactId}:${ext.previousVersion}"
                    )
                }
            }
        }

        tasks.register<Copy>(BUF_BREAKING_EXTRACT_TASK_NAME) {
            enabled = ext.previousVersion != null
            from(configurations.getByName(BUF_BREAKING_CONFIGURATION_NAME).files)
            into(file("$buildDir/$bufbuildBreaking"))
        }

        tasks.register<Exec>(BUF_BREAKING_TASK_NAME) {
            group = JavaBasePlugin.CHECK_TASK_NAME
            enabled = ext.previousVersion != null
            bufTask(
                ext,
                "breaking",
                "--against",
                "$relativeBuildDir/$bufbuildBreaking/${artifactDetails.get()?.artifactId}-${ext.previousVersion}.json"
            )
        }

        afterEvaluate {
            tasks.named(BUF_BREAKING_TASK_NAME).dependsOn(BUF_BREAKING_EXTRACT_TASK_NAME)
            tasks.named(BUF_BREAKING_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
            tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_BREAKING_TASK_NAME)
        }
    }

    companion object {
        private const val EXTRACT_INCLUDE_PROTO_TASK_NAME = "extractIncludeProto"

        const val BUF_BUILD_TASK_NAME = "bufBuild"
        const val BUF_BREAKING_EXTRACT_TASK_NAME = "bufBreakingExtract"
        const val BUF_BREAKING_TASK_NAME = "bufBreaking"
        const val BUF_LINT_TASK_NAME = "bufLint"

        const val BUF_BREAKING_CONFIGURATION_NAME = "bufBreaking"
        const val BUF_CONFIGURATION_NAME = "buf"
        const val BUF_IMAGE_PUBLICATION_NAME = "bufImagePublication"
        const val BUF_BUILD_DIR = "bufbuild"
    }
}

private fun Exec.bufTask(ext: BufExtension, vararg args: String) {
    commandLine("docker")
    setArgs(project.baseDockerArgs(ext) + args + bufTaskConfigOption(ext))
}

private fun Exec.bufTaskConfigOption(ext: BufExtension) =
    project.resolveConfig(ext).let {
        if (it != null) {
            logger.trace("Using buf config from $it")
            listOf("--config", it.readText())
        } else {
            logger.trace("Using buf config from default location if it exists (project directory)")
            emptyList()
        }
    }

private fun Project.resolveConfig(ext: BufExtension): File? =
    configurations.getByName(BUF_CONFIGURATION_NAME).files.let {
        if (it.isNotEmpty()) {
            require(it.size == 1) {
                "Buf lint configuration should only have one file; had $it"
            }
            it.single()
        } else {
            ext.configFileLocation
        }
    }

private fun Project.baseDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--volume", "$projectDir:/workspace",
        "--workdir", "/workspace",
        "bufbuild/buf:${ext.toolVersion}"
    )

private fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

private val Project.relativeBuildDir
    get() = buildDir.absolutePath.substringAfterLast(File.separator)
