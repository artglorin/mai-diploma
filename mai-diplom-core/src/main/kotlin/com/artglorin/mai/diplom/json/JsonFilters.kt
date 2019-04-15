package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 15/05/2018
 */

interface JsonFilter {
    fun pass(node: JsonNode): Boolean
}

class SimpleJsonFilter(private val matcher: JsonNodeMatcher, private val getter: JsonNodeGetter = SameGetter()) : JsonFilter {
    override fun pass(node: JsonNode): Boolean = matcher.match(getter.extract(node))
}


class AllJsonFilter(private val filters: Collection<JsonFilter>) : JsonFilter {
    override fun pass(node: JsonNode): Boolean = filters.all { it.pass(node) }
}

class AnyJsonFilter(private val filters: Collection<JsonFilter>) : JsonFilter {
    override fun pass(node: JsonNode): Boolean = filters.any { it.pass(node) }
}

class JsonFilterFactory {
    companion object {
        fun create(settings: ObjectNode): JsonFilter {
            val filterType = settings.get("type").textValue()
            return when (filterType) {
                "simple" -> SimpleJsonFilter(JsonNodeMatcherFactory.create(settings.get("matcher"))
                        , JsonNodeGetterFactory.create(settings.get("sourcePath").textValue()))

                "all" -> {
                    AllJsonFilter(
                            (settings.get("filters") as ArrayNode).map {
                                SimpleJsonFilter(JsonNodeMatcherFactory.create(it.get("matcher"))
                                        , JsonNodeGetterFactory.create(it.get("sourcePath").textValue()))
                            })
                }
                "any" -> {
                    AnyJsonFilter(
                            (settings.get("filters") as ArrayNode).map {
                                SimpleJsonFilter(JsonNodeMatcherFactory.create(it.get("matcher"))
                                        , JsonNodeGetterFactory.create(it.get("sourcePath").textValue()))
                            })
                }
                else -> throw IllegalArgumentException("Unsupported filter type: $filterType")
            }

        }
    }
}