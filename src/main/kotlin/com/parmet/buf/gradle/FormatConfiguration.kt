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
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

const val BUF_FORMAT_CHECK_TASK_NAME = "bufFormatCheck"
const val BUF_FORMAT_APPLY_TASK_NAME = "bufFormatApply"

internal fun Project.configureFormat() {
    configureBufFormatCheck()
    configureBufFormatApply()
}

private fun Project.configureBufFormatCheck() {
    tasks.register<FormatCheckTask>(BUF_FORMAT_CHECK_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that a Protobuf schema is formatted according to Buf's formatting rules."
        enabled = getExtension().enforceFormat
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_FORMAT_CHECK_TASK_NAME)
}

private fun Project.configureBufFormatApply() {
    tasks.register<FormatApplyTask>(BUF_FORMAT_APPLY_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Formats a Protobuf schema according to Buf's formatting rules."
    }
}
