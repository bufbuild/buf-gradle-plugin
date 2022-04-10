package com.parmet.buf.gradle

import org.gradle.api.Project

internal fun Project.usesWorkspaces() =
    file("buf.work.yaml").let { it.exists() && it.isFile }
