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
import java.nio.file.Paths

class FormatApplyWithProtobufGradleTest : AbstractFormatApplyTest() {
    @Test
    fun `format a bad separate protobuf source directory through the protobuf-gradle-plugin`() {
        assertSuccess(
            """
                // Copyright (c) 2022 Andrew Parmet
                //
                // Licensed under the Apache License, Version 2.0 (the "License");
                // you may not use this file except in compliance with the License.
                // You may obtain a copy of the License at
                // http://www.apache.org/licenses/LICENSE-2.0
                //
                // Unless required by applicable law or agreed to in writing, software
                // distributed under the License is distributed on an "AS IS" BASIS,
                // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                // See the License for the specific language governing permissions and
                // limitations under the License.
                
                syntax = "proto3";
                
                package parmet.buf.test.v1;
                
                message BasicMessage {}
    
            """.trimIndent(),
            Paths.get(projectDir.path, "subdir", "parmet", "buf", "test", "v1", "test.proto")
        )
    }

    @Test
    fun `format a bad file with a protobuf dependency and a google dependency with the protobuf-gradle-plugin`() {
        assertSuccess(
            """
                // Copyright (c) 2022 Andrew Parmet
                //
                // Licensed under the Apache License, Version 2.0 (the "License");
                // you may not use this file except in compliance with the License.
                // You may obtain a copy of the License at
                // http://www.apache.org/licenses/LICENSE-2.0
                //
                // Unless required by applicable law or agreed to in writing, software
                // distributed under the License is distributed on an "AS IS" BASIS,
                // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                // See the License for the specific language governing permissions and
                // limitations under the License.
                
                syntax = "proto3";
                
                package parmet.buf.test.v1;
                
                import "google/protobuf/any.proto";
                import "protokt/protokt.proto";
                
                message BasicMessage {
                  google.protobuf.Any any = 1;
                  protokt.ProtoktFileOptions protokt_file_options = 2;
                }
    
            """.trimIndent()
        )
    }

    override fun protoFile() =
        Paths.get(protoDir.path, "parmet", "buf", "test", "v1", "test.proto")
}
