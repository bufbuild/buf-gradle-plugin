/*
 * Copyright (c) 2021 Andrew Parmet
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

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

abstract class AbstractBufIntegrationTest {
    @TempDir
    lateinit var projectDir: File

    lateinit var settingsFile: File
    lateinit var buildFile: File
    lateinit var protoDir: File
    lateinit var configFile: File

    @BeforeEach
    fun setup() {
        settingsFile = projectDir.newFile("settings.gradle")
        buildFile = projectDir.newFile("build.gradle")
        protoDir = projectDir.newFolder("src", "main", "proto")
        configFile = projectDir.newFile("buf.yaml")

        settingsFile.writeText("rootProject.name = 'testing'")
    }

    class WrappedRunner(
        private val delegate: GradleRunner
    ) {
        fun withArguments(vararg args: String) =
            WrappedRunner(delegate.withArguments(*args))

        fun build() =
            delegate.build().also { println(it.output) }

        fun buildAndFail() =
            delegate.buildAndFail().also { println(it.output) }
    }

    fun gradleRunner() =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .let { WrappedRunner(it) }

    fun checkRunner() =
        gradleRunner().withArguments("check")

    fun publishRunner() =
        gradleRunner().withArguments("publish")
}
