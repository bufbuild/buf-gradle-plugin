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

import com.google.common.truth.Truth.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_TASK_NAME
import org.junit.jupiter.api.Test
import java.nio.file.Paths

abstract class AbstractGenerateTest : AbstractBufIntegrationTest() {
    @Test
    fun `generate java`() {
        gradleRunner().withArguments(BUILD_TASK_NAME).build()
    }

    @Test
    fun `generate java with kotlin dsl`() {
        gradleRunner().withArguments(BUILD_TASK_NAME).build()
    }

    @Test
    fun `generate java with --include-imports`() {
        gradleRunner().withArguments(BUILD_TASK_NAME).build()
        val generatedPathElements =
            listOf("build", "bufbuild", "generated", "java", "com", "google", "type", "DateTime.java")
        assertThat(Paths.get(projectDir.absolutePath, *generatedPathElements.toTypedArray()).toFile().exists()).isTrue()
    }

    @Test
    fun `generate java with gen file override`() {
        gradleRunner().withArguments(BUILD_TASK_NAME).build()
    }

    @Test
    fun `generate java with gen file override fails with no files`() {
        val result = gradleRunner().withArguments(BUILD_TASK_NAME).buildAndFail()
        assertThat(result.output).contains("No buf.gen.yaml file found in the root directory or with genFileLocation")
    }

    @Test
    fun `generate java with gen file override fails with two files`() {
        val result = gradleRunner().withArguments(BUILD_TASK_NAME).buildAndFail()
        assertThat(result.output).contains("Buf gen configuration file specified in the root directory as well as with genFileLocation")
    }
}
