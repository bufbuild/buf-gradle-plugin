package build.buf.gradle

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.gradle.api.GradleException
import java.io.File

class BufYamlGenerator {
    val yamlMapper = ObjectMapper(YAMLFactory()).registerKotlinModule()

    /**
     * Read the user-supplied buf.yaml file and generate an equivalent file, adding a
     * `modules` entry for each proto source directory.
     */
    fun generate(bufYamlFile: File?, protoDirs: List<String>): String {
        val bufYaml = bufYamlFile?.let {
            yamlMapper.readValue(it, object : TypeReference<MutableMap<String, Any>>() {})
        } ?: mutableMapOf("version" to "v2")

        // Copy existing buf.yaml and force version to v2.
        val newYaml = bufYaml.toMutableMap().apply {
            this["version"] = "v2" // Force v2
        }

        // Collect `breaking: ignore:` entries.
        val ignores = bufYaml["breaking"]?.let { breaking ->
            (breaking as? Map<*, *>)?.get("ignore") as? List<*>
        }?.map { it.toString() } ?: emptyList()

        // Emit a module for each discovered workspace, copying ignores and concatenating their
        // paths with the module root.
        val modules = protoDirs.map { dir ->
            if (ignores.isEmpty()) {
                mapOf("path" to dir)
            } else {
                mapOf(
                    "path" to dir,
                    "breaking" to mapOf("ignore" to ignores.map { "$dir/$it" })
                )
            }
        }
        newYaml["modules"] = modules
        return yamlMapper.writeValueAsString(newYaml)
    }
}