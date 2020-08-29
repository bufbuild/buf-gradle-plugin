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

        project.tasks.register<Exec>("bufCheckLint") {
            commandLine("docker")
            setArgs(
                listOf(
                    "run",
                    "--volume", "${project.projectDir}:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "check", "lint"
                )
            )
        }

        project.afterEvaluate {
            project.tasks.named("bufCheckLint").dependsOn("extractIncludeProto")
            project.tasks.named("check").dependsOn("bufCheckLint")
        }

        val bufBuildImage = "bufbuild/image.json"

        project.tasks.register<Exec>("bufImageBuild") {
            commandLine("docker")
            project.file("${project.buildDir}/bufbuild").mkdirs()
            setArgs(
                listOf(
                    "run",
                    "--volume", "${project.projectDir}:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "image", "build", "--output", "$${project.relativeBuildDir}/$bufBuildImage"
                )
            )
        }

        project.afterEvaluate {
            project.tasks.named("bufImageBuild").dependsOn("extractIncludeProto")
        }

        project.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("bufbuildImage") {
                    artifactId = "${project.name}-bufbuild"
                    artifact(project.file("${project.buildDir}/$bufBuildImage"))
                }
            }
        }

        val bufbuildBreaking = "bufbuild/breaking"

        project.configurations.create("bufCheckBreaking")
        project.dependencies {
            if (ext.previousVersion != null) {
                add("bufCheckBreaking", "${project.group}:${project.name}-bufbuild:${ext.previousVersion}")
            }
        }

        project.tasks.register<Copy>("bufCheckBreakingExtract") {
            enabled = ext.previousVersion != null
            from(project.configurations.getByName("bufCheckBreaking").files)
            into(project.file("${project.buildDir}/$bufbuildBreaking"))
        }

        project.tasks.register<Exec>("bufCheckBreaking") {
            enabled = ext.previousVersion != null
            commandLine("docker")
            setArgs(
                listOf(
                    "run",
                    "--volume", "${project.projectDir}:/workspace",
                    "--workdir", "/workspace",
                    "bufbuild/buf", "check", "breaking",
                    "--against-input", "${project.relativeBuildDir}/$bufbuildBreaking/${project.name}-bufbuild-${ext.previousVersion}.json"
                )
            )
        }

        project.afterEvaluate {
            project.tasks.named("bufCheckBreaking").dependsOn("extractIncludeProto")
            project.tasks.named("check").dependsOn("bufCheckBreaking")
        }
    }
}

fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

val Project.relativeBuildDir
    get() = buildDir.absolutePath.substringAfterLast("/")
