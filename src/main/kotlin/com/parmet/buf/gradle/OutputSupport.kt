package com.parmet.buf.gradle

import org.gradle.api.Task
import java.io.File

internal fun Task.createsOutput() {
    doFirst { File(project.bufbuildDir).mkdirs() }
}
