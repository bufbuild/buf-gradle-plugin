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

import org.gradle.api.Action
import java.io.File

open class BufExtension {
    /**
     * Specify the location of buf.yaml.
     */
    var configFileLocation: File? = null

    /**
     * Publish an image.json artifact with the complete descriptor set.
     */
    var publishSchema = false

    /**
     * Enables breakage checking against the image published with the provided version. In general prefer checking
     * against the latest release with [checkSchemaAgainstLatestRelease].
     */
    var previousVersion: String? = null

    /**
     * Checks the project's schema against the latest published schema. To check against a constant version use
     * [previousVersion].
     */
    var checkSchemaAgainstLatestRelease = false

    /**
     * Enables format checking via `buf format`. Apply formatting suggestions automatically with the `bufFormatApply`
     * task.
     */
    var enforceFormat = true

    /**
     * Specify the version of Buf.
     */
    var toolVersion = "1.27.1"

    internal var imageArtifactDetails: ArtifactDetails? = null

    /**
     * Specify the artifact details for schema publication.
     */
    fun imageArtifact(configure: Action<ArtifactDetails>) {
        imageArtifactDetails = (imageArtifactDetails ?: ArtifactDetails()).apply(configure::execute)
    }

    internal var generateOptions: GenerateOptions? = null

    /**
     * Generate code using `buf generate`. Configure any options with the provided [configure] closure.
     */
    fun generate(configure: Action<GenerateOptions>) {
        generateOptions = (generateOptions ?: GenerateOptions()).apply(configure::execute)
    }
}

class ArtifactDetails(
    var groupId: String? = null,
    var artifactId: String? = null,
    var version: String? = null,
)

class GenerateOptions(
    /**
     * If you specify any dependencies in `buf.yaml`, you must create a `buf.lock` file using `buf mod update` for
     * dependency resolution to succeed.
     */
    var includeImports: Boolean? = null,
    /**
     * Specify the location of buf.gen.yaml if not using one in the project root.
     */
    var templateFileLocation: File? = null,
)
