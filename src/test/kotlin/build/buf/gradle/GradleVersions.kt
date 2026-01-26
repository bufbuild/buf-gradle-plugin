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

/**
 * Defines Gradle versions to test against for backward compatibility.
 */
object GradleVersions {
    /**
     * Latest Gradle 8.x version - for backward compatibility testing.
     */
    const val GRADLE_8_LATEST = "8.14.4"

    /**
     * Represents "use wrapper version" - the current Gradle version is defined in
     * gradle/wrapper/gradle-wrapper.properties. When this value is passed to test methods,
     * the wrapper version will be used.
     */
    const val GRADLE_WRAPPER = "gradle-wrapper"

    /**
     * All Gradle versions to test against.
     */
    val ALL =
        listOf(
            GRADLE_8_LATEST,
            GRADLE_WRAPPER,
        )
}
