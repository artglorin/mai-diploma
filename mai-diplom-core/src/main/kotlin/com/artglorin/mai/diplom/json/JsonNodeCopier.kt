package com.artglorin.mai.diplom.json

import com.artglorin.mai.diplom.core.CopierConfig
import com.fasterxml.jackson.databind.JsonNode

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 16/05/2018
 */

interface JsonNodeCopier {
    fun transfer(from: JsonNode, to : JsonNode)
}

class SimpleJsonNodeCopier(
        private val getter: JsonNodeGetter,
        private val setter: JsonNodeSetter
) : JsonNodeCopier {
    override fun transfer(from: JsonNode, to: JsonNode) {
        setter.set(getter.extract(from).deepCopy(), to)
    }
}

class JsonNodeCopierFactory {
    companion object {
        fun create(settings: CopierConfig): JsonNodeCopier {
            return SimpleJsonNodeCopier(
                    JsonNodeGetterFactory.create(settings.from),
                    JsonNodeSetterFactory.create(settings.to))
        }
    }
}