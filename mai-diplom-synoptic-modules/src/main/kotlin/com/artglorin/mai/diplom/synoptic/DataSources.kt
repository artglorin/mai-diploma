package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.Customizable
import com.artglorin.mai.diplom.core.DataSourceModule
import com.artglorin.mai.diplom.core.JsonNodeListenersContainer
import com.artglorin.mai.diplom.json.JacksonNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class JsonDataSource : DataSourceModule, Settingable, JsonNodeObservable {
    private var sourceFile: Path? = null
    private var idGenerator = AtomicInteger()

    override fun applySettings(settings: JsonNode) {
        sourceFile = settings.get("sourceFile")
                ?.textValue()
                ?.let { ClassPathResource(it).file.toPath() }
                ?: throw IllegalArgumentException("sourceFile must be present in settings")
    }
}
