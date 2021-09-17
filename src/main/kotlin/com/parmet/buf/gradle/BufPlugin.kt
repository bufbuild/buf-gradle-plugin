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

import com.parmet.buf.gradle.BufPlugin.Companion.BUF_BUILD_DIR
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
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
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<BufExtension>("buf")
        project.configurations.create(BUF_CONFIGURATION_NAME)

        project.afterEvaluate {
            project.configureWriteWorkspaceYaml()
            project.configureCopyProtoToWorkspace()
            project.configureLint(ext)
            project.getArtifactDetails(ext)?.let {
                if (ext.publishSchema) {
                    project.configureBuild(ext, it)
                }
                if (ext.runBreakageCheck()) {
                    project.configureBreaking(ext, it)
                }
            }
        }
    }

    private fun Project.configureLint(ext: BufExtension) {
        tasks.register<Exec>(BUF_LINT_TASK_NAME) {
            group = CHECK_TASK_NAME
            bufTask(ext, "lint")
        }

        tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
    }

    private fun Project.getArtifactDetails(ext: BufExtension): ArtifactDetails? {
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

    private fun Project.configureBuild(ext: BufExtension, artifactDetails: ArtifactDetails) {
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

    private fun Project.configureBreaking(ext: BufExtension, artifactDetails: ArtifactDetails) {
        addSchemaDependency(ext, artifactDetails)

        val bufBreakingFile = LazyBufBreakingFile()
        configureSchemaExtraction(bufBreakingFile)
        configureBreakingTask(ext, bufBreakingFile)

        tasks.named(CHECK_TASK_NAME).dependsOn(BUF_BREAKING_TASK_NAME)
    }

    private fun Project.addSchemaDependency(ext: BufExtension, artifactDetails: ArtifactDetails) {
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

        configurations.create(BUF_BREAKING_CONFIGURATION_NAME)

        dependencies {
            add(BUF_BREAKING_CONFIGURATION_NAME, "${artifactDetails.groupAndArtifact()}:$versionSpecifier")
        }
    }

    private fun Project.configureSchemaExtraction(bufBreakingFile: LazyBufBreakingFile) {
        tasks.register<Copy>(BUF_BREAKING_EXTRACT_TASK_NAME) {
            outputs.upToDateWhen { false }
            val breakingDir = file("$bufbuildDir/$BREAKING_DIR")

            doFirst { breakingDir.deleteRecursively() }

            from(configurations.getByName(BUF_BREAKING_CONFIGURATION_NAME).files)
            into(breakingDir)

            doLast {
                val copiedFiles = breakingDir.listFiles().orEmpty()

                val fileName =
                    checkNotNull(copiedFiles.singleOrNull()) {
                        "Unable to resolve a single file from Buf schema publication. Found $copiedFiles. Please " +
                            "file an issue at https://github.com/andrewparmet/buf-gradle-plugin/issues/new if you " +
                            "see this error."
                    }.name

                logger.info("Buf will check schema dependency against $fileName")

                bufBreakingFile.fileName = fileName
            }
        }
    }

    private fun Project.configureBreakingTask(ext: BufExtension, bufBreakingFile: LazyBufBreakingFile) {
        tasks.register<Exec>(BUF_BREAKING_TASK_NAME) {
            dependsOn(BUF_BREAKING_EXTRACT_TASK_NAME)
            group = CHECK_TASK_NAME

            bufTask(
                ext,
                "breaking",
                "--against",
                bufBreakingFile
            )
        }
    }

    private fun Exec.bufTask(ext: BufExtension, vararg args: Any) {
        dependsOn(COPY_PROTO_TO_WORKSPACE_TASK_NAME)
        dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)

        commandLine("docker")
        setArgs(project.baseDockerArgs(ext) + args + bufTaskConfigOption(ext))
    }

    private fun Project.configureWriteWorkspaceYaml() {
        tasks.register(WRITE_WORKSPACE_YAML_TASK_NAME) {
            outputs.dir(bufbuildDir)
            doLast {
                File(bufbuildDir).mkdirs()
                File("$bufbuildDir/buf.work.yaml").writeText(
                    """
                        version: v1
                        directories:
                          - workspace
                    """.trimIndent()
                )
            }
        }
    }

    private fun Project.configureCopyProtoToWorkspace() {
        tasks.register<Copy>(COPY_PROTO_TO_WORKSPACE_TASK_NAME) {
            dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)

            from("src/main/proto", "build/extracted-include-protos/main")
            into("${project.bufbuildDir}/$WORKSPACE_DIR")
        }
    }

    private fun Exec.bufTaskConfigOption(ext: BufExtension) =
        project.resolveConfig(ext).let {
            if (it != null) {
                logger.info("Using buf config from $it")
                listOf("--config", it.readText())
            } else {
                val configFile = project.file("buf.yaml")
                if (configFile.exists()) {
                    logger.info("Using buf config from default location (project directory)")
                    listOf("--config", configFile.readText())
                } else {
                    logger.info("Using default buf config")
                    emptyList()
                }
            }
        }

    private fun Project.resolveConfig(ext: BufExtension): File? =
        configurations.getByName(BUF_CONFIGURATION_NAME).let {
            if (it.dependencies.isNotEmpty()) {
                check(ext.configFileLocation == null) {
                    "Buf lint configuration specified with a config file location and a dependency; pick one."
                }
                checkNotNull(it.files.singleOrNull()) {
                    "Buf lint configuration should have exactly one file; had ${it.files}."
                }
            } else {
                ext.configFileLocation
            }
        }

    private class LazyBufBreakingFile(var fileName: String? = null) {
        override fun toString() =
            "$BREAKING_DIR/$fileName"
    }

    companion object {
        private const val EXTRACT_INCLUDE_PROTO_TASK_NAME = "extractIncludeProto"

        const val BUF_BUILD_TASK_NAME = "bufBuild"
        const val BUF_BREAKING_EXTRACT_TASK_NAME = "bufBreakingExtract"
        const val BUF_BREAKING_TASK_NAME = "bufBreaking"
        const val BUF_LINT_TASK_NAME = "bufLint"
        const val WRITE_WORKSPACE_YAML_TASK_NAME = "writeWorkspaceYaml"
        const val COPY_PROTO_TO_WORKSPACE_TASK_NAME = "copyProtoToWorkspace"

        const val BUF_BREAKING_CONFIGURATION_NAME = "bufBreaking"
        const val BUF_CONFIGURATION_NAME = "buf"
        const val BUF_IMAGE_PUBLICATION_NAME = "bufImagePublication"

        const val BUF_BUILD_DIR = "bufbuild"
        const val BREAKING_DIR = "breaking"
        const val WORKSPACE_DIR = "workspace"

        const val BUF_BUILD_PUBLICATION_FILENAME = "image.json"
    }
}

private fun Project.baseDockerArgs(ext: BufExtension) =
    listOf(
        "run",
        "--volume", "$bufbuildDir:/workspace:Z",
        "--workdir", "/workspace",
        "bufbuild/buf:${ext.toolVersion}"
    )

private fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

private val Project.bufbuildDir
    get() = "$buildDir/$BUF_BUILD_DIR"

private fun BufExtension.runBreakageCheck() =
    checkSchemaAgainstLatestRelease || previousVersion != null

private fun ArtifactDetails.groupAndArtifact() =
    "$groupId:$artifactId"
