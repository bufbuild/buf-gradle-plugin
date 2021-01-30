package com.parmet.buf.gradle

import java.io.File
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

        project.afterEvaluate {
            project.configureLint(ext)
            project.getArtifactDetails(ext)?.let {
                if (ext.publishSchema) {
                    project.configureBuild(ext, it)
                }
                if (ext.previousVersion != null) {
                    project.configureBreaking(ext, it)
                }
            }
        }
    }

    private fun Project.configureLint(ext: BufExtension) {
        tasks.register<Exec>(BUF_LINT_TASK_NAME) {
            dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)

            group = JavaBasePlugin.CHECK_TASK_NAME
            bufTask(ext, "lint")
        }

        tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
    }

    private fun Project.getArtifactDetails(ext: BufExtension): ArtifactDetails? =
        if (ext.publishSchema || ext.previousVersion != null) {
            val publications = the<PublishingExtension>().publications.withType<MavenPublication>()
            ext.imageArtifactDetails
                ?: publications.singleOrNull()?.let {
                    ArtifactDetails(
                        it.groupId,
                        "${it.artifactId}-$BUF_IMAGE_PUBLICATION_NAME",
                        it.version
                    )
                } ?: error(
                    "Unable to determine image artifact details and schema publication or " +
                        "compatibility check was requested; no image publication details " +
                        "were provided and there was not exactly one publication from which " +
                        "to infer them (found ${publications.size}). Either configure the " +
                        "plugin with imageArtifact() or configure a publication."
                )
        } else {
            null
        }

    private fun Project.configureBuild(ext: BufExtension, artifactDetails: ArtifactDetails) {
        logger.info("Publishing buf schema image to ${artifactDetails.groupAndArtifact()}:${artifactDetails.version}")

        val bufBuildImage = "$BUF_BUILD_DIR/image.json"

        tasks.register<Exec>(BUF_BUILD_TASK_NAME) {
            dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)

            doFirst { file("$buildDir/$BUF_BUILD_DIR").mkdirs() }
            bufTask(ext, "build", "--output", "$relativeBuildDir/$bufBuildImage")
        }

        the<PublishingExtension>().publications {
            create<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
                groupId = artifactDetails.groupId
                artifactId = artifactDetails.artifactId
                version = artifactDetails.version

                artifact(file("$buildDir/$bufBuildImage")) {
                    builtBy(tasks.named(BUF_BUILD_TASK_NAME))
                }
            }
        }
    }

    private fun Project.configureBreaking(ext: BufExtension, artifactDetails: ArtifactDetails) {
        logger.info("Resolving buf schema image from ${artifactDetails.groupAndArtifact()}:${ext.previousVersion}")

        val bufbuildBreaking = "$BUF_BUILD_DIR/breaking"

        configurations.create(BUF_BREAKING_CONFIGURATION_NAME)
        dependencies {
            add(
                BUF_BREAKING_CONFIGURATION_NAME,
                "${artifactDetails.groupId}:${artifactDetails.artifactId}:${ext.previousVersion}"
            )
        }

        tasks.register<Copy>(BUF_BREAKING_EXTRACT_TASK_NAME) {
            from(configurations.getByName(BUF_BREAKING_CONFIGURATION_NAME).files)
            into(file("$buildDir/$bufbuildBreaking"))
        }

        tasks.register<Exec>(BUF_BREAKING_TASK_NAME) {
            dependsOn(BUF_BREAKING_EXTRACT_TASK_NAME)
            dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)

            group = JavaBasePlugin.CHECK_TASK_NAME
            bufTask(
                ext,
                "breaking",
                "--against",
                "$relativeBuildDir/$bufbuildBreaking/${artifactDetails.artifactId}-${ext.previousVersion}.json"
            )
        }

        tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_BREAKING_TASK_NAME)
    }

    private fun Exec.bufTask(ext: BufExtension, vararg args: String) {
        commandLine("docker")
        setArgs(project.baseDockerArgs(ext) + args + bufTaskConfigOption(ext))
    }

    private fun Exec.bufTaskConfigOption(ext: BufExtension) =
        project.resolveConfig(ext).let {
            if (it != null) {
                logger.info("Using buf config from $it")
                listOf("--config", it.readText())
            } else {
                logger.info("Using buf config from default location if it exists (project directory)")
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
