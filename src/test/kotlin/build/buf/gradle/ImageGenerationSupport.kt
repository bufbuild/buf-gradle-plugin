package build.buf.gradle

import com.google.common.collect.Lists
import org.junit.jupiter.params.provider.Arguments

private val NULL_SENTINEL = Any()

object ImageGenerationSupport {
    @JvmStatic
    fun publicationFileExtensionTestCase() =
        Lists
            .cartesianProduct(
                ImageFormat.values().map { it.formatName },
                CompressionFormat.values().map { it.ext } + NULL_SENTINEL,
            ).map { imageAndCompression ->
                Arguments.of(imageAndCompression[0], imageAndCompression[1].takeIf { it != NULL_SENTINEL })
            }

    fun AbstractBufIntegrationTest.replaceBuildDetails(
        format: String,
        compression: String?,
    ) {
        buildFile.replace(
            "imageFormat = REPLACEME",
            "imageFormat = '$format'",
        )
        buildFile.replace(
            "compressionFormat = REPLACEME",
            "compressionFormat = ${compression?.let { "'$it'" }}",
        )
    }
}
