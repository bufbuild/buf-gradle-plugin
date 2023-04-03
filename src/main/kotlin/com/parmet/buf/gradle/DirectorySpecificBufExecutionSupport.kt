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



package com.parmet.buf.gradle

import org.gradle.api.Task
import java.nio.file.Path

internal fun Task.execBufInSpecificDirectory(
    vararg bufCommand: String,
    customErrorMessage: ((String) -> String)? = null
) {
    execBufInSpecificDirectory(bufCommand.asList(), emptyList(), customErrorMessage)
}

internal fun Task.execBufInSpecificDirectory(
    bufCommand: String,
    extraArgs: Iterable<String>,
    customErrorMessage: ((String) -> String)
) {
    execBufInSpecificDirectory(listOf(bufCommand), extraArgs, customErrorMessage)
}

private fun Task.execBufInSpecificDirectory(
    bufCommand: Iterable<String>,
    extraArgs: Iterable<String>,
    customErrorMessage: ((String) -> String)? = null
) {
    fun runWithArgs(path: Path? = null) =
        bufCommand + listOfNotNull(path?.let(::mangle)) + extraArgs

    when {
        project.hasProtobufGradlePlugin() ->
            project.srcProtoDirs().forEach { execBuf(runWithArgs(it), customErrorMessage) }
        project.hasWorkspace() ->
            execBuf(bufCommand, customErrorMessage)
        else ->
            execBuf(runWithArgs(), customErrorMessage)
    }
}
