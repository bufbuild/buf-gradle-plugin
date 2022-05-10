/*
 * Copyright (c) 2022 Andrew Parmet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.parmet.buf.gradle

import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP
import java.nio.file.Path

const val BUF_FORMAT_CHECK_TASK_NAME = "bufFormatCheck"
const val BUF_FORMAT_APPLY_TASK_NAME = "bufFormatApply"

internal fun Project.configureFormat() {
    if (getExtension().enforceFormat) {
        configureBufFormatCheck()
    }
    configureBufFormatApply()
}

private fun Project.configureBufFormatCheck() {
    tasks.register(BUF_FORMAT_CHECK_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that a Protobuf schema is formatted according to Buf's formatting rules."

        fun formatWithArgs(path: Path? = null) =
            listOfNotNull("format", "-d", "--exit-code", path?.let(::mangle))

        when {
            hasProtobufGradlePlugin() ->
                srcProtoDirs().forEach { execBuf(formatWithArgs(it)) }
            hasWorkspace() ->
                execBuf("format", "-d", "--exit-code")
            else ->
                execBuf(formatWithArgs())
        }
    }
}

private fun Project.configureBufFormatApply() {
    tasks.register(BUF_FORMAT_APPLY_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Formats a Protobuf schema according to Buf's formatting rules."

        fun formatWithArgs(path: Path? = null) =
            listOfNotNull("format", "-w", path?.let(::mangle))

        when {
            hasProtobufGradlePlugin() ->
                srcProtoDirs().forEach { execBuf(formatWithArgs(it)) }
            hasWorkspace() ->
                execBuf("format", "-w")
            else ->
                execBuf(formatWithArgs())
        }
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_FORMAT_CHECK_TASK_NAME)
}
