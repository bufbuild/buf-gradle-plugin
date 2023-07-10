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

import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome.SUCCESS
import org.junit.jupiter.api.Test
import java.nio.file.Path

abstract class AbstractFormatApplyTest : AbstractBufIntegrationTest() {
    @Test
    fun `format an incorrect message`() {
        assertSuccess(
            """
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
                
                syntax = "proto3";
                
                package buf.test.v1;
                
                message Foo {}
                
            """.trimIndent(),
        )
    }

    protected fun assertSuccess(after: String, protoFile: Path = protoFile()) {
        assertThat(
            gradleRunner()
                .withArguments(BUF_FORMAT_APPLY_TASK_NAME)
                .build()
                .task(":$BUF_FORMAT_APPLY_TASK_NAME")
                ?.outcome,
        ).isEqualTo(SUCCESS)

        assertThat(protoFile.toFile().readText()).isEqualTo(after)
    }

    protected abstract fun protoFile(): Path
}
