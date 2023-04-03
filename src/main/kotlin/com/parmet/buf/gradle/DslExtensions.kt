

package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure

fun Project.buf(cfg: BufExtension.() -> Unit) =
    configure(cfg)

fun DependencyHandler.buf(dependencyNotation: Any): Dependency? =
    add(BUF_CONFIGURATION_NAME, dependencyNotation)
