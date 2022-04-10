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
import org.junit.jupiter.api.Test

interface ConfigOverrideableLintTests : LintTestUtilities {
    @Test
    fun `lint with a config in default location`() {
        assertSuccess()
    }

    @Test
    fun `lint with a file location config override`() {
        assertSuccess()
    }

    @Test
    fun `lint with a dependency config override`() {
        assertSuccess()
    }

    @Test
    fun `lint with a dependency config override fails with two files`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("buf-1.yaml")
        assertThat(result.output).contains("buf-2.yaml")
    }

    @Test
    fun `lint with a dependency config override fails with no files`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("Buf lint configuration should have exactly one file")
        assertThat(result.output).contains("had []")
    }

    @Test
    fun `lint with a file and dependency config override fails`() {
        val result = checkRunner().buildAndFail()
        assertThat(result.output).contains("config file location and a dependency; pick one")
    }
}
