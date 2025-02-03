package build.buf.gradle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

internal class BufYamlGenerator {
    private val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    /**
     * Read the user-supplied buf.yaml file and generate an equivalent file, adding a
     * `modules` entry for each proto source directory.
     */
    fun generate(
        bufYamlFile: File?,
        protoDirs: List<String>,
    ): String {
        val bufYaml = bufYamlFile?.let { mapper.readValue<Map<String, Any>>(it) }.orEmpty().toMutableMap()
        bufYaml["version"] = "v2"

        // Collect `breaking: ignore:` entries.
        val ignores =
            bufYaml["breaking"]
                ?.let { breaking ->
                    (breaking as? Map<*, *>)?.get("ignore") as? List<*>
                }?.map { it.toString() } ?: emptyList()

        // Emit a module for each discovered workspace, copying ignores and concatenating their
        // paths with the module root.
        val modules =
            protoDirs.map { dir ->
                if (ignores.isEmpty()) {
                    mapOf("path" to dir)
                } else {
                    mapOf(
                        "path" to dir,
                        "breaking" to mapOf("ignore" to ignores.map { "$dir/$it" }),
                    )
                }
            }
        bufYaml["modules"] = modules
        return mapper.writeValueAsString(bufYaml)
    }
}
