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

import io.github.g00fy2.versioncompare.Version
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.plugins.AppliedPlugin
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
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

internal const val BUF_CLI_V2_INITIAL_VERSION = "1.32.0"

private val BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN =
    listOf("build", "extracted-include-protos", "main").joinToString(File.separator)

private val BUILD_EXTRACTED_PROTOS_MAIN =
    listOf("build", "extracted-protos", "main").joinToString(File.separator)

internal fun Project.hasProtobufGradlePlugin() = pluginManager.hasPlugin("com.google.protobuf")

internal fun Project.bufV1SyntaxOnly() = Version(getExtension().toolVersion) < Version(BUF_CLI_V2_INITIAL_VERSION)

internal fun Project.withProtobufGradlePlugin(action: (AppliedPlugin) -> Unit) = pluginManager.withPlugin("com.google.protobuf", action)

internal fun Project.configureCreateSymLinksToModules() {
    registerBufTask<CreateSymLinksToModulesTask>(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME) {
        workspaceCommonConfig()
        bufbuildDir.set(project.bufbuildDir)
        candidateProtoDirs.setFrom(allProtoDirs())
    }
}

abstract class CreateSymLinksToModulesTask : AbstractBufTask() {
    /** Buf output directory. Should be set to [BUF_BUILD_DIR]. */
    @get:OutputDirectory
    internal abstract val bufbuildDir: Property<File>

    /** Directories possibly containing input .proto files. */
    @get:InputFiles
    internal abstract val candidateProtoDirs: ConfigurableFileCollection

    @TaskAction
    fun createSymLinksToModules() {
        val bufbuildDirValue = bufbuildDir.get()
        candidateProtoDirs
            .filter { anyProtos(it) }
            .forEach {
                val symLinkFile = File(bufbuildDirValue, makeMangledRelativizedPathStr(it))
                if (!symLinkFile.exists()) {
                    logger.info("Creating symlink for $it at $symLinkFile")
                    Files.createSymbolicLink(
                        symLinkFile.toPath(),
                        bufbuildDirValue.toPath().relativize(it.toPath()),
                    )
                }
            }
    }
}

internal fun Project.configureWriteWorkspaceYaml() {
    registerBufTask<WriteWorkspaceYamlTask>(WRITE_WORKSPACE_YAML_TASK_NAME) {
        workspaceCommonConfig()
        projectDir.set(project.projectDir)
        candidateProtoDirs.setFrom(allProtoDirs())
        if (project.bufV1SyntaxOnly()) {
            v1SyntaxOnly.set(true)
            outputFile.set(File(project.bufbuildDir, "buf.work.yaml"))
        } else {
            v1SyntaxOnly.set(false)
            outputFile.set(File(project.bufbuildDir, "buf.yaml"))
            bufConfigFile.set(project.bufConfigFile())
        }
    }
}

abstract class WriteWorkspaceYamlTask : AbstractBufTask() {
    @get:Input
    internal abstract val v1SyntaxOnly: Property<Boolean>

    /** Directories possibly containing input .proto files. */
    @get:InputFiles
    @get:Optional
    internal abstract val candidateProtoDirs: ConfigurableFileCollection

    /** The input buf configuration file. */
    @get:InputFile
    @get:Optional
    internal abstract val bufConfigFile: Property<File>

    /** Output yaml file. */
    @get:OutputFile
    internal abstract val outputFile: Property<File>

    @TaskAction
    fun writeWorkspaceYaml() {
        if (v1SyntaxOnly.get()) {
            val bufWork =
                """
                |version: v1
                |directories:
                ${workspaceSymLinkEntries()}
                """.trimMargin()

            logger.info("Writing generated buf.work.yaml:\n$bufWork")
            outputFile.get().writeText(bufWork)
        } else {
            val protoDirs =
                candidateProtoDirs
                    .filter { anyProtos(it) }
                    .map { makeMangledRelativizedPathStr(it) }
            val bufYaml = BufYamlGenerator().generate(bufConfigFile.orNull, protoDirs)
            logger.info("Writing generated buf.yaml:{}\n", bufYaml)
            outputFile.get().writeText(bufYaml)
        }
    }
}

private fun Task.workspaceCommonConfig() {
    dependsOn(
        project
            .tasks
            .matching { it::class.java.name == "com.google.protobuf.gradle.ProtobufExtract_Decorated" },
    )
}

private fun WriteWorkspaceYamlTask.workspaceSymLinkEntries() =
    candidateProtoDirs
        .filter { anyProtos(it) }
        .map { makeMangledRelativizedPathStr(it) }
        .joinToString("\n") { "|  - $it" }

// Returns all directories that have may have proto files relevant to processing the project's proto files. This
// includes any proto files that are simply references (includes) as well as those that will be processed (code
// generation or validation).
private fun Task.allProtoDirs() =
    project
        .allProtoSourceSetDirs()
        .plus(project.file(Paths.get(BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN)))
        .toSet()

// Returns the list of directories containing proto files defined in *this* project. The returned directories do *not*
// include those that contain protos defined in dependencies and placed in the extracted-protos directory for codegen.
// The returned directories *only* contain the proto files that should be processed by buf for operations like linting,
// format checking, and breaking change detection.
//
// NOTE: We explicitly remove BUILD_EXTRACTED_PROTOS_MAIN from the list of source set directories in order to fix issue
// https://github.com/bufbuild/buf-gradle-plugin/issues/132. Starting in version 0.9.2 of
// protobuf-gradle-plugin, the "proto" source set srcDirs includes the outputs of the "extractedProtos" task, which
// breaks this plugin when used with the protobuf-gradle-plugin and "protobuf" dependencies (which generates code for
// any proto files within those dependencies).
//
// Protobuf-gradle-plugin change that introduced this behavior: https://github.com/google/protobuf-gradle-plugin/pull/637/
// Line: https://github.com/google/protobuf-gradle-plugin/blob/9d2a328a0d577bf4439d3b482a953715b3a03027/src/main/groovy/com/google/protobuf/gradle/ProtobufPlugin.groovy#L425
internal fun Project.projectDefinedProtoDirs() = allProtoSourceSetDirs() - file(Paths.get(BUILD_EXTRACTED_PROTOS_MAIN))

// Returns deduplicated list of all proto source set directories.
private fun Project.allProtoSourceSetDirs() = projectProtoSourceSetDirs() + androidProtoSourceSetDirs()

// Returns android proto source set directories that protobuf-gradle-plugin will codegen.
private fun Project.androidProtoSourceSetDirs() =
    extensions
        .findByName("android")
        ?.let { baseExtension ->
            val prop = baseExtension::class.declaredMemberProperties.single { it.name == "sourceSets" }
            @Suppress("UNCHECKED_CAST")
            (prop as KProperty1<Any, Set<ExtensionAware>>).get(baseExtension)
        }.orEmpty()
        .flatMap { it.projectProtoSourceSetDirs() }
        .toSet()

// Returns all proto source set directories that the protobuf-gradle-plugin will codegen.
private fun Project.projectProtoSourceSetDirs() =
    the<SourceSetContainer>()
        .flatMap { it.projectProtoSourceSetDirs() }
        .toSet()

// Returns all directories within the "proto" source set of the receiver that actually contain proto files. This includes
// directories explicitly added to the source set, as well as directories containing files from "protobuf" dependencies.
private fun ExtensionAware.projectProtoSourceSetDirs() =
    extensions
        .getByName<SourceDirectorySet>("proto")
        .srcDirs
        .toSet()

internal fun AbstractBufTask.makeMangledRelativizedPathStr(file: File) = mangle(projectDir.get().toPath().relativize(file.toPath()))

// Indicates if the specified directory contains any proto files.
internal fun anyProtos(directory: File) = directory.walkTopDown().any { it.extension == "proto" }

private fun mangle(name: Path) = name.toString().replace("-", "--").replace(File.separator, "-")

internal inline fun <reified T : AbstractBufTask> Project.registerBufTask(
    name: String,
    noinline configuration: T.() -> Unit,
): TaskProvider<T> =
    tasks.register<T>(name) {
        projectDir.set(project.projectDir)
        configuration()
    }

internal inline fun <reified T : AbstractBufExecTask> Project.registerBufExecTask(
    name: String,
    noinline configuration: T.() -> Unit,
): TaskProvider<T> =
    registerBufTask<T>(name) {
        bufExecutable.setFrom(configurations.getByName(BUF_BINARY_CONFIGURATION_NAME))
        if (project.hasProtobufGradlePlugin()) {
            hasProtobufGradlePlugin.set(true)
            candidateProtoDirs.setFrom(projectDefinedProtoDirs())
            workingDir.set(project.bufbuildDir)
            // whenever this task is part of the build, we must run it before any buf execution
            mustRunAfter(COPY_BUF_CONFIG_TASK_NAME)
        } else {
            hasProtobufGradlePlugin.set(false)
            workingDir.set(project.projectDir)
        }
        hasWorkspace.set(project.hasWorkspace())
        configuration()
    }.also { taskProvider ->
        withProtobufGradlePlugin {
            afterEvaluate {
                taskProvider.dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
                taskProvider.dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
            }
        }
    }
