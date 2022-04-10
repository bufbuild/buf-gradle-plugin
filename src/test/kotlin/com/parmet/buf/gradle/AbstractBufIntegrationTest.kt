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

import com.google.common.truth.Truth.assertWithMessage
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

abstract class AbstractBufIntegrationTest : IntegrationTest {
    @TempDir
    lateinit var projectDir: File

    private val settingsFile
        get() = File(projectDir, "settings.gradle")

    val buildFile
        get() = File(projectDir, "build.gradle").takeIf { it.exists() } ?: File(projectDir, "build.gradle.kts")

    val protoDir
        get() = Paths.get(projectDir.path, "src", "main", "proto").toFile()

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        settingsFile.writeText("rootProject.name = 'testing'")

        val fixture = File("src/test/resources/${testInfo.testClass.get().simpleName}/${testInfo.testMethod.get().name}")
        assertWithMessage("Failed to copy test fixture files").that(fixture.copyRecursively(projectDir)).isTrue()
    }

    @AfterEach
    fun inspectTempDir() {
        println("ls -lR".runCommand(File(projectDir, "build")))
    }

    private fun String.runCommand(workingDir: File): String {
        val parts = split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
            .directory(workingDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    }

    class WrappedRunner(
        private val delegate: GradleRunner
    ) {
        fun withArguments(vararg args: String) =
            WrappedRunner(delegate.withArguments(delegate.arguments + args))

        fun build() =
            delegate.build().also { println(it.output) }

        fun buildAndFail() =
            delegate.buildAndFail().also { println(it.output) }
    }

    fun gradleRunner() =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("-PprotobufGradleVersion=0.8.18", "-PprotobufVersion=3.19.4")
            .let { WrappedRunner(it) }

    override fun checkRunner() =
        gradleRunner().withArguments("check")

    fun publishRunner() =
        gradleRunner().withArguments("publish")
}

interface IntegrationTest {
    fun checkRunner(): AbstractBufIntegrationTest.WrappedRunner
}
