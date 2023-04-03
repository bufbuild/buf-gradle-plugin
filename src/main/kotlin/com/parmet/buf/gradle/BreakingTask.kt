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

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class BreakingTask : DefaultTask() {
    @TaskAction
    fun bufBreaking() {
        execBuf(
            "breaking",
            bufBuildPublicationFile,
            "--against",
            singleFileFromConfiguration(BUF_BREAKING_CONFIGURATION_NAME)
        ) {
            """
                |Some Protobuf files had breaking changes:
                |$it
            """.trimMargin()
        }
    }
}
