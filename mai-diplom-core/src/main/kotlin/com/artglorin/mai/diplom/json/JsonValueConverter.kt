package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode

interface JsonValueConverter {
    fun transfer(from: JsonNode, to: JsonNode): Boolean
}

class SingleJsonValueConverter(private val nodeGetter: JsonNodeGetter,
                               private val matcher: JsonNodeMatcher,
                               private val nodeSetter: JsonNodeSetter,
                               private val valueNode: JsonNode,
                               private val defaultNode: JsonNode? = null,
                               private val required: Boolean = false) : JsonValueConverter {

    override fun transfer(from: JsonNode, to: JsonNode): Boolean {
        val sourceNode = nodeGetter.extract(from)
        return when {
            matcher.match(sourceNode) -> {
                nodeSetter.set(valueNode.deepCopy(), to); return true
            }
            defaultNode != null -> {
                nodeSetter.set(defaultNode.deepCopy(), to); return true;
            }
            else -> if (required) throw CannotConvertValue(from, nodeGetter.path, matcher.toString()) else false
        }
    }
}

class ComplexJsonValueConverters(private val others: Collection<JsonValueConverter>) : JsonValueConverter {
    override fun transfer(from: JsonNode, to: JsonNode): Boolean {
        return others.any { it.transfer(from, to) }
    }
}

internal object JsonValueConverterFactory {
    fun create(settings: JsonNode): JsonValueConverter {

        val sourceGetter = JsonNodeGetterFactory.create(settings.path("sourcePath").asText())
        val targetSetter = JsonNodeSetterFactory.create(settings.path("targetPath").asText())
        val matchNode = settings.get("matchValue")
        val misMatchNode = settings.get("mismatchValue")
        val matcherNode = settings.get("matcher")
        return if ("complex" == matcherNode.get("name").textValue()) {
            ComplexJsonValueConverters((matcherNode.get("matchers") as ArrayNode)
                    .map {
                        val matcher = JsonNodeMatcherFactory.create(it)
                        val matchValue = it.get("matchValue")
                        SingleJsonValueConverter(sourceGetter, matcher, targetSetter, matchValue
                                ?: matchNode, misMatchNode)
                    })
        } else {
            val nodeMatcher = JsonNodeMatcherFactory.create(matcherNode)
            val matchValue = matchNode.get("matchValue")
            SingleJsonValueConverter(sourceGetter, nodeMatcher, targetSetter, matchValue ?: matchNode, misMatchNode)
        }
    }
}