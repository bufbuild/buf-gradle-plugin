package com.parmet.buf.gradle

import org.junit.jupiter.api.Test

class LintWithProtobufGradleTest : AbstractLintTest() {
    @Test
    fun `lint a separate protobuf source directory through the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with a protobuf dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }

    @Test
    fun `lint a file with a protobuf dependency and a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess()
    }
}
