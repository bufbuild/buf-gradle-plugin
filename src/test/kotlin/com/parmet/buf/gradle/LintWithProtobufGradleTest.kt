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

import org.junit.jupiter.api.Test

class LintWithProtobufGradleTest : ConfigOverrideableLintTests, AbstractLintTest() {
    @Test
    fun `lint with protobuf plugin applied after buf plugin`() {
        assertSuccess()
    }

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
