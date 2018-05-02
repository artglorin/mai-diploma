package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.core.io.ClassPathResource
import java.net.URI
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import javax.annotation.PostConstruct

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java)
}

@SpringBootApplication
open class Application {

    @PostConstruct
    fun init() {
        val loadProperties = PropertiesLoader.APP_CONFIG.loadProperties()
        val modulesDir = loadProperties.path(ConfigKeys.MODULES_DIR).asText(FilesAndFolders.MODULES_DIR)
        if (StringUtils.isBlank(modulesDir) || Files.exists(Paths.get(modulesDir)).not()) {
            throw RequiredModulesNotLoaded("data source modules not loaded")
        }
            val dataSource = Paths.get(modulesDir).resolve(FilesAndFolders.DATA_SOURCES_MODULE_DIR)
//                    .resolve("dataSource")
            Files.list(dataSource)
                    .filter({ it.fileName.toString().endsWith(".jar") })
                    .map { it.toUri().toURL() }
                    .collect(Collectors.toList())
                    .apply {
                        val loader = URLClassLoader(this.toTypedArray())
                        val load = ServiceLoader.load(DataSourceModule::class.java, loader)
                        load.forEach({
                            println(it.javaClass.name)
                        })
//                        loader.
                    }

    }
}


class PropertiesLoader(private val source: () -> URI) {
    companion object {
        val APP_CONFIG = PropertiesLoader({
            val configFile = Paths.get(System.getProperty("user.dir"), "app_config.json")
            if (Files.exists(configFile)) {
                return@PropertiesLoader configFile.toUri()
            } else {
                val resource = ClassPathResource("app_config.json")
                if (resource.exists()) {
                    return@PropertiesLoader resource.uri
                } else {
                    return@PropertiesLoader Files.createTempFile(null, null).toUri()
                }
            }
        })
    }

    private var loaded = false
    private var properties: JsonNode? = null
    @Synchronized
    fun loadProperties(): JsonNode {
        if (!loaded) {
            try {
                source().toURL().openStream().reader().use {
                    val mapper = ObjectMapper()
                    properties = mapper.readTree(it) ?: mapper.createObjectNode().nullNode()
                }
            } catch (e: Exception) {
                throw e
            }
            loaded = true
        }
        return properties!!
    }
}