

package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertWithMessage
import org.gradle.language.base.plugins.LifecycleBasePlugin.CHECK_TASK_NAME
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

abstract class AbstractBufIntegrationTest : IntegrationTest {
    @TempDir
    lateinit var projectDir: File

    val buildFile
        get() = File(projectDir, "build.gradle").takeIf { it.exists() } ?: File(projectDir, "build.gradle.kts")

    val protoDir
        get() = Paths.get(projectDir.path, "src", "main", "proto").toFile()

    @BeforeEach
    fun setup(testInfo: TestInfo) {
        File(projectDir, "settings.gradle").writeText("rootProject.name = 'testing'")
        File(projectDir, "gradle.properties").writeText("org.gradle.jvmargs=-Xmx5g")

        val fixture = File("src/test/resources/${testInfo.testClass.get().simpleName}/${testInfo.testMethod.get().name}")
        assertWithMessage("Failed to copy test fixture files").that(fixture.copyRecursively(projectDir)).isTrue()
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
            .withArguments(
                "-PprotobufGradleVersion=0.8.19",
                "-PprotobufVersion=3.21.7",
                "-PkotlinVersion=1.7.20",
                "-PandroidGradleVersion=7.3.0"
            )
            .let { WrappedRunner(it) }

    override fun checkRunner() =
        gradleRunner().withArguments(CHECK_TASK_NAME)

    fun publishRunner() =
        gradleRunner().withArguments("publish")

    fun File.replace(oldValue: String, newValue: String) {
        writeText(readText().replace(oldValue, newValue))
    }
}

interface IntegrationTest {
    fun checkRunner(): AbstractBufIntegrationTest.WrappedRunner
}

fun String.osIndependent() =
    replace("\n", lineSeparator)
