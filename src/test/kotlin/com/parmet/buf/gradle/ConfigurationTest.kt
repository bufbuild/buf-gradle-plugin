package com.parmet.buf.gradle

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ConfigurationTest : AbstractBufIntegrationTest() {
    @Test
    fun `project cannot use both workspaces and the protobuf-gradle-plugin, protobuf applied first`() {
        assertFailure()
    }

    @Test
    fun `project cannot use both workspaces and the protobuf-gradle-plugin, protobuf applied second`() {
        assertFailure()
    }

    private fun assertFailure() {
        val result = gradleRunner().withArguments(":tasks").buildAndFail()
        assertThat(result.output).contains("cannot use both the protobuf-gradle-plugin and Buf workspaces")
    }
}
