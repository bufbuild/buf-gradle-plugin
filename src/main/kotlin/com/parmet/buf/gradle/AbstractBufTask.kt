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

import org.gradle.api.DefaultTask

abstract class AbstractBufTask : DefaultTask() {
    init {
        if (project.hasProtobufGradlePlugin()) {
            // todo: see if this can be abstracted differently so we don't call an
            // overrideable function from the constructor
            dependsOn(CREATE_SYM_LINKS_TO_MODULES_TASK_NAME)
            dependsOn(WRITE_WORKSPACE_YAML_TASK_NAME)
        }
    }
}
