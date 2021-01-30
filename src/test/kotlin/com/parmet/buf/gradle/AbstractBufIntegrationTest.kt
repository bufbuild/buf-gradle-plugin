package com.parmet.buf.gradle

import java.io.File
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
}
