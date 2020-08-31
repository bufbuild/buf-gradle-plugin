package com.parmet.buf.gradle

import com.parmet.buf.gradle.BufPlugin.Companion.BUF_IMAGE_PUBLICATION_NAME
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
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<BufExtension>("buf")
        project.configureCheckLint()
        project.configureImageBuild(ext)
        project.configureCheckBreaking(ext)
    }

    private fun Project.configureCheckLint() {
        tasks.register<Exec>(BUF_CHECK_LINT_TASK_NAME) {
            group = JavaBasePlugin.CHECK_TASK_NAME
            bufTask("check", "lint")
        }

        afterEvaluate {
            tasks.named(BUF_CHECK_LINT_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
            tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_CHECK_LINT_TASK_NAME)
        }
    }

    private fun Project.configureImageBuild(ext: BufExtension) {
        val bufBuildImage = "$BUF_BUILD_DIR/image.json"

        tasks.register<Exec>(BUF_IMAGE_BUILD_TASK_NAME) {
            doFirst {
                file("$buildDir/$BUF_BUILD_DIR").mkdirs()
            }
            bufTask(
                "image",
                "build",
                "--output",
                "$relativeBuildDir/$bufBuildImage"
            )
        }

        afterEvaluate {
            tasks.named(BUF_IMAGE_BUILD_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
        }

        afterEvaluate {
            if (ext.publishSchema) {
                the<PublishingExtension>().publications {
                    withType<MavenPublication> {
                        val groupId = groupId
                        val artifactId = artifactId
                        val version = version

                        // hack
                        if (name != BUF_IMAGE_PUBLICATION_NAME) {
                            create<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
                                this.groupId = groupId
                                this.artifactId = "$artifactId-$BUF_IMAGE_PUBLICATION_NAME"
                                this.version = version

                                artifact(file("$buildDir/$bufBuildImage")) {
                                    builtBy(tasks.named(BUF_IMAGE_BUILD_TASK_NAME))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Project.configureCheckBreaking(ext: BufExtension) {
        val bufbuildBreaking = "$BUF_BUILD_DIR/breaking"

        configurations.create(BUF_CHECK_BREAKING_CONFIGURATION_NAME)
            dependencies {
                afterEvaluate {
                    if (ext.previousVersion != null) {
                        withBufPublication {
                            add(BUF_CHECK_BREAKING_CONFIGURATION_NAME, "$groupId:$artifactId:${ext.previousVersion}")
                        }
                    }
                }
            }

        tasks.register<Copy>(BUF_CHECK_BREAKING_EXTRACT_TASK_NAME) {
            enabled = ext.previousVersion != null
            from(configurations.getByName(BUF_CHECK_BREAKING_CONFIGURATION_NAME).files)
            into(file("$buildDir/$bufbuildBreaking"))
        }

        // hack
        var artifactName: String? = null
        afterEvaluate {
            withBufPublication {
                artifactName = artifactId
            }
        }

        tasks.register<Exec>(BUF_CHECK_BREAKING_TASK_NAME) {
            group = JavaBasePlugin.CHECK_TASK_NAME
            enabled = ext.previousVersion != null
            bufTask(
                "check",
                "breaking",
                "--against-input",
                "$relativeBuildDir/$bufbuildBreaking/$artifactName-${ext.previousVersion}.json"
            )
        }

        afterEvaluate {
            tasks.named(BUF_CHECK_BREAKING_TASK_NAME).dependsOn(BUF_CHECK_BREAKING_EXTRACT_TASK_NAME)
            tasks.named(BUF_CHECK_BREAKING_TASK_NAME).dependsOn(EXTRACT_INCLUDE_PROTO_TASK_NAME)
            tasks.named(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(BUF_CHECK_BREAKING_TASK_NAME)
        }
    }

    companion object {
        private const val EXTRACT_INCLUDE_PROTO_TASK_NAME = "extractIncludeProto"

        const val BUF_IMAGE_BUILD_TASK_NAME = "bufImageBuild"
        const val BUF_CHECK_BREAKING_EXTRACT_TASK_NAME = "bufCheckBreakingExtract"
        const val BUF_CHECK_BREAKING_TASK_NAME = "bufCheckBreaking"
        const val BUF_CHECK_LINT_TASK_NAME = "bufCheckLint"

        const val BUF_CHECK_BREAKING_CONFIGURATION_NAME = "bufCheckBreaking"
        const val BUF_IMAGE_PUBLICATION_NAME = "bufbuild"
        const val BUF_BUILD_DIR = "bufbuild"
    }
}

private fun Exec.bufTask(vararg args: String) {
    commandLine("docker")
    setArgs(project.baseDockerArgs + args)
}

private val Project.baseDockerArgs
    get() = listOf(
        "run",
        "--volume", "$projectDir:/workspace",
        "--workdir", "/workspace",
        "bufbuild/buf"
    )

private fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

private val Project.relativeBuildDir
    get() = buildDir.absolutePath.substringAfterLast("/")

private fun Project.withBufPublication(
    configuration: MavenPublication.() -> Unit
) {
    the<PublishingExtension>().publications {
        named<MavenPublication>(BUF_IMAGE_PUBLICATION_NAME) {
            configuration()
        }
    }
}
