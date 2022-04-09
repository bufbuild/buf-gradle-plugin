package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.the
import java.nio.file.Path
import java.nio.file.Paths

private const val BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN = "build/extracted-include-protos/main"
private const val BUILD_EXTRACTED_PROTOS_MAIN = "build/extracted-protos/main"

fun Project.allProtoDirs(): List<Path> =
    (srcProtoDirs() + extractProtoDirs()).filter { anyProtos(it) }

fun Project.srcProtoDirs() =
    the<SourceSetContainer>()["main"]
        .extensions
        .getByName("proto")
        .let { it as SourceDirectorySet }
        .srcDirs
        .map { projectDir.toPath().relativize(it.toPath()) }
        .filter { anyProtos(it) }

private fun extractProtoDirs() =
    listOf(
        BUILD_EXTRACTED_INCLUDE_PROTOS_MAIN,
        BUILD_EXTRACTED_PROTOS_MAIN
    ).map(Paths::get)

private fun Project.anyProtos(path: Path) =
    file(path).walkTopDown().any { it.extension == "proto" }
