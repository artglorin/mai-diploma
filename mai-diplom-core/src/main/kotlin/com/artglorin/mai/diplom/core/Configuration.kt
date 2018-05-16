package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.error
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.MissingNode
import com.fasterxml.jackson.databind.node.ObjectNode
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
        var modulesPath: String = FilesAndFolders.MODULES_DIR,
        var modules: Map<String, ModuleConfig> = emptyMap(),
        var dataFlow: List<FlowItem> = emptyList(),
        var pipes: List<PipeConfiguration> = emptyList()
)

data class FlowItem (
        var moduleId : String = "",
        var inputId: String = "",
        var outputId: String = "",
        var wholeSeries: Boolean = false
)

data class PipeConfiguration(
        var inputId: List<String> = emptyList(),
        var outputId: List<String> = emptyList(),
        var filters: List<JsonNode> = emptyList(),
        var copiers: List<CopierConfig> = emptyList(),
        var converters: List<ConverterDescription> = emptyList()

)

data class ConverterDescription(
        var sourcePath: String = "",
        var targetPath: String = "",
        var matchValue: JsonNode = MissingNode.getInstance(),
        var mismatchValue: JsonNode = MissingNode.getInstance(),
        var matcherId : String = "",
        var matcherSettings : JsonNode = MissingNode.getInstance()
)

data class CopierConfig(
        var template: JsonNode = MissingNode.getInstance(),
        var paths: List<ObjectNode> = emptyList()
)

object ConfigurationLogger {
    val LOG = LoggerFactory.getLogger(ConfigurationLogger::class.java.name)!!
}

fun Configuration.configure(modules: List<Module>) {
    modules.filter { it -> it.javaClass.kotlin.isInstance(Customizable::class) }
            .forEach { configure(it) }
}

fun Configuration.configure(module: Module) {
    val moduleId = module.getModuleId()
    modules[moduleId]?.apply {
        if (module is Customizable) {
            ConfigurationLogger.LOG.debug("Apply settings got module '$moduleId'")
            module.applySettings(this.settings)
        }
    }
}

data class ModuleConfig(var settings: JsonNode = MissingNode.getInstance(),
                        var id: String = ""
)

class ConfigurationLoader(private val source: () -> URI?) {
    companion object {
        val LOG = LoggerFactory.getLogger(ConfigurationLoader::class.java.name)!!
        val APP_CONFIG = ConfigurationLoader({
            val configFile = Paths.get(System.getProperty("user.dir"), FilesAndFolders.CONFIG_FILE)
            if (Files.exists(configFile)) {
                LOG.debug("${FilesAndFolders.CONFIG_FILE} was loaded from user.dir")
                return@ConfigurationLoader configFile.toUri()
            } else {
                val resource = ClassPathResource(FilesAndFolders.CONFIG_FILE)
                if (resource.exists()) {
                    LOG.debug("${FilesAndFolders.CONFIG_FILE} was loaded from classpath")
                    return@ConfigurationLoader resource.uri
                } else {
                    LOG.debug("${FilesAndFolders.CONFIG_FILE} was not found")
                    return@ConfigurationLoader null
                }
            }
        })
    }

    private var loaded = false
    private var configuration: Configuration? = null
    @Synchronized
    fun loadProperties(): Configuration {
        if (!loaded) {
            val url = source()?.toURL()
            if (url != null) {
                try {
                    LOG.debug("Loading Configuration from url : '$url'")
                    val mapper = ObjectMapper()
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    configuration = mapper.readValue(url, Configuration::class.java)
                    if (configuration == null) {
                        LOG.error(::ConfigurationNotLoaded, "Configuration was not loaded. May be ${FilesAndFolders.CONFIG_FILE} is have wrong structure. Loaded from $url")
                    }
                } catch (e: Exception) {
                    LOG.error(::ConfigurationNotLoaded, "Configuration was not loaded. May be ${FilesAndFolders.CONFIG_FILE} is have wrong structure.Loaded from $url. Error: $e")
                }
            }
            loaded = true
        }

        return configuration ?: Configuration()

    }
}