package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.net.URI
import java.nio.file.Files
import java.nio.file.Paths

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 03/05/2018
 */
data class Configuration(
        var modules: Modules = Modules()
)

data class Modules (
        var path: String = FilesAndFolders.MODULES_DIR,
        val dataSources: List<ModuleConfig> = emptyList(),
        val taskManager: ModuleConfig = ModuleConfig(),
        val solutionModule: ModuleConfig = ModuleConfig(),
        val dataHandlers: List<ModuleConfig> = emptyList(),
        val dataObservers: List<ModuleConfig> = emptyList()

)

data class ModuleConfig(var jarName: String = "",
                        var settings: JsonNode = ObjectMapper().createObjectNode().nullNode(),
                        var id: String = ""
)

class ConfigurationLoader(private val source: () -> URI) {
    companion object {
        val LOG = LoggerFactory.getLogger(Application::class.java.name)!!
        val APP_CONFIG = ConfigurationLoader({
            val configFile = Paths.get(System.getProperty("user.dir"), FilesAndFolders.CONFIG_FILE)
            if (Files.exists(configFile)) {
                return@ConfigurationLoader configFile.toUri()
            } else {
                val resource = ClassPathResource(FilesAndFolders.CONFIG_FILE)
                if (resource.exists()) {
                    return@ConfigurationLoader resource.uri
                } else {
                    return@ConfigurationLoader Files.createTempFile(null, null).toUri()
                }
            }
        })
    }

    private var loaded = false
    private var configuration: Configuration? = null
    @Synchronized
    fun loadProperties(): Configuration {
        if (!loaded) {
            try {
                val mapper = ObjectMapper()
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                configuration = mapper.readValue(source().toURL(), Configuration::class.java)
                if (configuration == null) {
                    LOG.error(::ConfigurationNotLoaded, "Configuration was not loaded. May be ${FilesAndFolders.CONFIG_FILE} is have wrong structure")
                }
            } catch (e: Exception) {
                LOG.error(::ConfigurationNotLoaded, "Configuration was not loaded. May be ${FilesAndFolders.CONFIG_FILE} is have wrong structure. Error: $e")
            }
            loaded = true
        }

        return configuration!!

    }
}