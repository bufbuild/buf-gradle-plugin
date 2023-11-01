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
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.dependencies

internal fun TaskProvider<*>.dependsOn(obj: Any) {
    configure { dependsOn(obj) }
}

internal fun Project.createConfigurationWithDependency(
    configuration: String,
    notation: Any,
) {
    configurations.create(configuration)
    dependencies { add(configuration, notation) }
}

internal fun Project.singleFileFromConfiguration(configuration: String) = configurations.getByName(configuration).singleFile

internal fun Task.singleFileFromConfiguration(configuration: String) = project.singleFileFromConfiguration(configuration)
