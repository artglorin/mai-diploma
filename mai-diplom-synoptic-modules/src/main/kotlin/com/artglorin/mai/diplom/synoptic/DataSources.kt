package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.Customizable
import com.artglorin.mai.diplom.core.DataSourceModule
import com.artglorin.mai.diplom.core.JsonNodeListenersContainer
import com.artglorin.mai.diplom.json.JacksonNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class JsonDataSource : DataSourceModule, Customizable {

    private val listeners = lazy {
        JsonNodeListenersContainer()
    }

    override fun getOutputSchema(): ObjectNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun addListener(listener: Consumer<ObjectNode>) {
        listeners.value.addObserver(listener)
    }

    override fun launch() {
        val mapper = ObjectMapper()
        if (listeners.isInitialized()) {
            Files
                    .lines(sourceFile)
                    .map { mapper.readTree(it) }
                    .map { JacksonNodeFactory.createModuleResult(getModuleId(), idGenerator.incrementAndGet().toString(), it) }
                    .forEach {
                        listeners.value.notify(it)
                    }
        }
    }

    private var sourceFile: Path? = null
    private var idGenerator = AtomicInteger()

    override fun applySettings(settings: JsonNode) {
        sourceFile = settings.get("sourceFile")
                ?.textValue()
                ?.let { ClassPathResource(it).file.toPath() }
                ?: throw IllegalArgumentException("sourceFile must be present in settings")
    }
}
