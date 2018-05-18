package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.json.JsonFilter
import com.artglorin.mai.diplom.json.JsonFilterFactory
import com.artglorin.mai.diplom.json.JsonValueConverter
import com.artglorin.mai.diplom.json.JsonValueConverterFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 16/05/2018
 */
class PipeFactory {
    companion object {
        fun create(config: PipeConfiguration): Pipe {
            return Pipe(
                    config.id,
                    config.filter?.let { JsonFilterFactory.create(it as ObjectNode) },
                    config.template,
                    config.converters.map { JsonValueConverterFactory.create(it) }
            )
        }
    }
}


class Pipe(
        val id: String,
        private val filters: JsonFilter? = null,
        private val template: JsonNode? = null,
        private val converters: List<JsonValueConverter> = emptyList()
) {
    private val listeners = lazy {
        JsonNodeListenersContainer()
    }

    fun push(node: ObjectNode) {
        if (listeners.isInitialized()) {
            if (filters != null && filters.pass(node).not()) {
                return
            }
            var result: ObjectNode = node
            if (converters.isNotEmpty()) {
                result = template?.deepCopy() ?: node.deepCopy()
                converters.forEach { it.transfer(node, result) }
            }
            listeners.value.notify(result)
        }
    }

    fun addListener(listener: Consumer<ObjectNode>) {
        listeners.value.addObserver(listener)
    }
}
