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

import com.google.common.truth.Truth.assertWithMessage
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import java.util.Locale
import java.util.regex.Pattern

abstract class AbstractBufIntegrationTest : IntegrationTest {
    @TempDir
    lateinit var projectDir: File

    // If true, allow this plugin to be invoked as a jar from the test project directory.
    //
    // 1. run `./gradlew jar` to build the plugin jar file.
    // 2. Set a breakpoint in DefaultGradleRunner.run(Action<GradleExecutionResult>) at call to `this.gradleExecutor.run`
    // 3. run the test to that breakpoint and note the test project directory (this.projectDirectory)
    // 4. stop the test run
    // 2. cd to the test project directory and run Gradle, appending the args `-Dorg.gradle.debug=true --no-daemon`
    // 3. set breakpoints and invoke the remote JVM debugger.
    private val enableRemoteDebug = false

    val buildFile
        get() = File(projectDir, "build.gradle").takeIf { it.exists() } ?: File(projectDir, "build.gradle.kts")

    val protoDir
        get() = Paths.get(projectDir.path, "src", "main", "proto").toFile()

    fun findPluginVersion(path: String): String? {
        val startDir = File(path)
        val regex = """buf-gradle-plugin-(.*?)-SNAPSHOT\.jar"""
        val pattern = Pattern.compile(regex)

        startDir.walkTopDown().forEach { file ->
            if (file.isFile && file.name.matches(pattern.toRegex())) {
                val matcher = pattern.matcher(file.name)
                if (matcher.find()) {
                    return matcher.group(1)
                }
            }
        }

        return null
    }

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        if (enableRemoteDebug) {
            val path = Paths.get("").toAbsolutePath().toString()
            val pluginVersion = findPluginVersion(path)
            File(projectDir, "settings.gradle").writeText(
                """
pluginManagement {
    buildscript {
        repositories {
            flatDir {
                dirs '${path}/build/libs'
            }
        }
        dependencies {
            classpath ':buf-gradle-plugin-${pluginVersion}-SNAPSHOT:${pluginVersion}'
        }
    }
}

rootProject.name = 'testing'
"""
            )
        } else {
            File(projectDir, "settings.gradle").writeText("rootProject.name = 'testing'")
        }
        File(projectDir, "gradle.properties").writeText("org.gradle.jvmargs=-Xmx5g")

        val testName =
            testInfo.testMethod.get().name
                .replace(",", "")
                .replace("--", "")
                .replace(" ", "_")
                .replace("-", "_")
                .lowercase(Locale.US)
        val fixture = File("src/test/resources/${testInfo.testClass.get().simpleName}/$testName")
        assertWithMessage("Directory ${fixture.path} does not exist").that(fixture.exists()).isTrue()
        assertWithMessage("Failed to copy test fixture files").that(fixture.copyRecursively(projectDir)).isTrue()
    }

    class WrappedRunner(
        private val delegate: GradleRunner,
    ) {
        fun withArguments(vararg args: String) = WrappedRunner(delegate.withArguments(delegate.arguments + args))

        fun build() = delegate.build().also { println(it.output) }

        fun buildAndFail() = delegate.buildAndFail().also { println(it.output) }
    }

    fun gradleRunner() =
        GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments(
                "-PprotobufGradleVersion=0.9.3",
                "-PprotobufVersion=3.23.4",
                "-PkotlinVersion=1.7.20",
                "-PandroidGradleVersion=7.3.0",
            )
            .let { WrappedRunner(it) }

    override fun checkRunner() = gradleRunner().withArguments(CHECK_TASK_NAME)

    fun publishRunner() = gradleRunner().withArguments("publish")

    fun File.replace(
        oldValue: String,
        newValue: String,
    ) {
        writeText(readText().replace(oldValue, newValue))
    }
}

interface IntegrationTest {
    fun checkRunner(): AbstractBufIntegrationTest.WrappedRunner
}

fun String.osIndependent() = replace("\n", lineSeparator)
