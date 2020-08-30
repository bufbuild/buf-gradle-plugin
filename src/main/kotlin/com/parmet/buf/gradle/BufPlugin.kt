package com.parmet.buf.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register

class BufPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val ext = project.extensions.create<BufExtension>("buf")
        project.configureCheckLint()
        project.configureImageBuild(ext)
        project.configureCheckBreaking(ext)
    }

    private fun Project.configureCheckLint() {
        tasks.register<Exec>("bufCheckLint") {
            group = "check"
            commandLine("docker")
            setArgs(
                listOf(
                    "run",
                    "--volume", "$projectDir:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "check", "lint"
                )
            )
        }

        afterEvaluate {
            tasks.named("bufCheckLint").dependsOn("extractIncludeProto")
            tasks.named("check").dependsOn("bufCheckLint")
        }
    }

    private fun Project.configureImageBuild(ext: BufExtension) {
        val bufBuildImage = "bufbuild/image.json"

        tasks.register<Exec>("bufImageBuild") {
            commandLine("docker")
            file("$buildDir/bufbuild").mkdirs()
            setArgs(
                listOf(
                    "run",
                    "--volume", "$projectDir:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "image", "build", "--output", "$$relativeBuildDir/$bufBuildImage"
                )
            )
        }

        afterEvaluate {
            tasks.named("bufImageBuild").dependsOn("extractIncludeProto")
        }

        if (ext.publishSchema) {
            configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("bufbuildImage") {
                        artifactId = "$name-bufbuild"
                        artifact(file("$buildDir/$bufBuildImage"))
                    }
                }
            }
        }
    }

    private fun Project.configureCheckBreaking(ext: BufExtension) {
        val bufbuildBreaking = "bufbuild/breaking"

        configurations.create("bufCheckBreaking")
        dependencies {
            if (ext.previousVersion != null) {
                add("bufCheckBreaking", "$group:$name-bufbuild:${ext.previousVersion}")
            }
        }

        tasks.register<Copy>("bufCheckBreakingExtract") {
            enabled = ext.previousVersion != null
            from(configurations.getByName("bufCheckBreaking").files)
            into(file("$buildDir/$bufbuildBreaking"))
        }

        tasks.register<Exec>("bufCheckBreaking") {
            group = "check"
            enabled = ext.previousVersion != null
            commandLine("docker")
            setArgs(
                listOf(
                    "run",
                    "--volume", "$projectDir:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "check", "breaking",
                    "--against-input", "$relativeBuildDir/$bufbuildBreaking/$name-bufbuild-${ext.previousVersion}.json"
                )
            )
        }

        afterEvaluate {
            tasks.named("bufCheckBreaking").dependsOn("extractIncludeProto")
            tasks.named("check").dependsOn("bufCheckBreaking")
        }
    }
}

private fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

private val Project.relativeBuildDir
    get() = buildDir.absolutePath.substringAfterLast("/")
