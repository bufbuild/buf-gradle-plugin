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

import org.gradle.api.Project
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.language.base.plugins.LifecycleBasePlugin.VERIFICATION_GROUP

const val BUF_LINT_TASK_NAME = "bufLint"

internal fun Project.configureLint() {
    registerBufExecTask<LintTask>(BUF_LINT_TASK_NAME) {
        group = VERIFICATION_GROUP
        description = "Checks that a Protobuf schema conforms to the Buf lint configuration."

        bufConfigFile.set(project.bufConfigFile())
        inputFiles.setFrom(obtainDefaultProtoFileSet())
    }

    tasks.named(CHECK_TASK_NAME).dependsOn(BUF_LINT_TASK_NAME)
}
