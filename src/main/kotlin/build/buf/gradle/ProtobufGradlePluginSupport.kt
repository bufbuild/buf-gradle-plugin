// Copyright 2023 Buf Technologies, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package build.buf.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getByName
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.the
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

const val CREATE_SYM_LINKS_TO_MODULES_TASK_NAME = "createSymLinksToModules"
const val WRITE_WORKSPACE_YAML_TASK_NAME = "writeWorkspaceYaml"

private val BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN =
    listOf("build", "extracted-include-protos", "main").joinToString(File.separator)

private val BUILD_EXTRACTED_PROTOS_MAIN =
    listOf("build", "extracted-protos", "main").joinToString(File.separator)

internal fun Project.hasProtobufGradlePlugin() =
    pluginManager.hasPlugin("com.google.protobuf")

internal fun Project.withProtobufGradlePlugin(action: (AppliedPlugin) -> Unit) =
    pluginManager.withPlugin("com.google.protobuf", action)

internal fun Project.configureCreateSymLinksToModules() {
    tasks.register<CreateSymLinksToModulesTask>(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME) {
        workspaceCommonConfig()
    }
}

abstract class CreateSymLinksToModulesTask : DefaultTask() {
    @TaskAction
    fun createSymLinksToModules() {
        allProtoDirs().forEach {
            val symLinkFile = File(bufbuildDir, mangle(it))
            if (!symLinkFile.exists()) {
                logger.info("Creating symlink for $it at $symLinkFile")
                Files.createSymbolicLink(
                    symLinkFile.toPath(),
                    bufbuildDir.toPath().relativize(project.file(it).toPath())
                )
            }
        }
    }
}

internal fun Project.configureWriteWorkspaceYaml() {
    tasks.register<WriteWorkspaceYamlTask>(WRITE_WORKSPACE_YAML_TASK_NAME) {
        workspaceCommonConfig()
    }
}

abstract class WriteWorkspaceYamlTask : DefaultTask() {
    @TaskAction
    fun writeWorkspaceYaml() {
        val bufWork =
            """
                |version: v1
                |directories:
                ${workspaceSymLinkEntries()}
            """.trimMargin()

        logger.info("Writing generated buf.work.yaml:\n$bufWork")

        File(bufbuildDir, "buf.work.yaml").writeText(bufWork)
    }
}

private fun Task.workspaceCommonConfig() {
    dependsOn(
        project
            .tasks
            .matching { it::class.java.name == "com.google.protobuf.gradle.ProtobufExtract_Decorated" }
    )
    createsOutput()
}

private fun Task.workspaceSymLinkEntries() =
    allProtoDirs().joinToString("\n") { "|  - ${mangle(it)}" }

private fun Task.allProtoDirs(): List<Path> =
    (project.srcProtoDirs() + extractProtoDirs()).filter { project.anyProtos(it) }

// NOTE: The hard coded extractProtoDirs are removed from the source set directories in order to fix issue
// https://github.com/bufbuild/buf-gradle-plugin/issues/132. Starting in version 0.9.2 of
// protobuf-gradle-plugin, the "proto" source set srcDirs includes the outputs of the "extractedProtos" task, which
// breaks this plugin when used with the protobuf-gradle-plugin and "protobuf" dependencies.
//
// Protobuf-gradle-plugin change that introduced this behavior: https://github.com/google/protobuf-gradle-plugin/pull/637/
// Line: https://github.com/google/protobuf-gradle-plugin/blob/9d2a328a0d577bf4439d3b482a953715b3a03027/src/main/groovy/com/google/protobuf/gradle/ProtobufPlugin.groovy#L425
internal fun Project.srcProtoDirs() =
    (the<SourceSetContainer>().flatMap { it.protoDirs(this) } + androidSrcProtoDirs())
        .minus(extractProtoDirs())

private fun Project.androidSrcProtoDirs() =
    extensions.findByName("android")
        ?.let { baseExtension ->
            val prop = baseExtension::class.declaredMemberProperties.single { it.name == "sourceSets" }
            @Suppress("UNCHECKED_CAST")
            (prop as KProperty1<Any, Set<ExtensionAware>>).get(baseExtension)
        }
        .orEmpty()
        .flatMap { it.protoDirs(this) }

private fun ExtensionAware.protoDirs(project: Project) =
    extensions.getByName<SourceDirectorySet>("proto").srcDirs
        .map { project.projectDir.toPath().relativize(it.toPath()) }
        .filter { project.anyProtos(it) }

private fun extractProtoDirs() =
    listOf(
        BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN,
        BUILD_EXTRACTED_PROTOS_MAIN
    ).map(Paths::get)

private fun Project.anyProtos(path: Path) =
    file(path).walkTopDown().any { it.extension == "proto" }

internal fun mangle(name: Path) =
    name.toString().replace("-", "--").replace(File.separator, "-")

internal inline fun <reified T : Task> Project.registerBufTask(
    name: String,
    noinline configuration: T.() -> Unit
): TaskProvider<T> {
    val taskProvider = tasks.register(name, configuration)
    withProtobufGradlePlugin {
        afterEvaluate {
            taskProvider.dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
            taskProvider.dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
        }
    }
    return taskProvider
}
