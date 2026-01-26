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

import org.assertj.core.api.Assertions.assertThat
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths
import java.util.Locale

abstract class AbstractBufIntegrationTest : IntegrationTest {
    @TempDir(cleanup = CleanupMode.ALWAYS)
    lateinit var projectDir: File

    val buildFile
        get() = File(projectDir, "build.gradle").takeIf { it.exists() } ?: File(projectDir, "build.gradle.kts")

    val protoDir
        get() = Paths.get(projectDir.path, "src", "main", "proto").toFile()

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        File(projectDir, "settings.gradle").writeText("rootProject.name = 'testing'")
        File(projectDir, "gradle.properties").writeText("org.gradle.jvmargs=-Xmx5g")

        val testName =
            testInfo.testMethod
                .get()
                .name
                .replace(",", "")
                .replace("--", "")
                .replace(" ", "_")
                .replace("-", "_")
                .lowercase(Locale.US)
        val fixture = File("src/test/resources/${testInfo.testClass.get().simpleName}/$testName")
        assertThat(fixture.exists()).`as`("Directory ${fixture.path} does not exist").isTrue()
        assertThat(fixture.copyRecursively(projectDir)).`as`("Failed to copy test fixture files").isTrue()
    }

    class WrappedRunner(
        private val delegate: GradleRunner,
    ) {
        fun withArguments(vararg args: String) = WrappedRunner(delegate.withArguments(delegate.arguments + args))

        fun build() = delegate.build().also { println(it.output) }

        fun buildAndFail() = delegate.buildAndFail().also { println(it.output) }
    }

    fun gradleRunner(version: String = GradleVersions.GRADLE_WRAPPER) =
        GradleRunner
            .create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .apply {
                // Use specific Gradle version if set, otherwise use wrapper version
                if (version != GradleVersions.GRADLE_WRAPPER) {
                    withGradleVersion(version)
                }
            }.withArguments(
                "-PprotobufGradleVersion=$PROTOBUF_GRADLE_PLUGIN_VERSION",
                "-PprotobufVersion=$PROTOBUF_VERSION",
                "-PkotlinVersion=$KOTLIN_VERSION",
                "-PandroidGradleVersion=$ANDROID_GRADLE_PLUGIN_VERSION",
                "--configuration-cache",
            ).withDebug(false) // Enable for interactive debugging
            .let { WrappedRunner(it) }

    override fun checkRunner(version: String) = gradleRunner(version).withArguments(CHECK_TASK_NAME)

    fun publishRunner() = gradleRunner().withArguments("publish")

    fun File.replace(
        oldValue: String,
        newValue: String,
    ) {
        writeText(readText().replace(oldValue, newValue))
    }
}

interface IntegrationTest {
    fun checkRunner(version: String = GradleVersions.GRADLE_WRAPPER): AbstractBufIntegrationTest.WrappedRunner
}

fun String.osIndependent() = replace("\n", lineSeparator)
