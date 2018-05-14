package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode

class JsonValueConverter(private val nodeGetter: JsonFieldGetter,
                         private val matcher: JsonNodeMatcher,
                         private val nodeSetter: JsonFieldSetter,
                         private val valueNode: JsonNode,
                         private val defaultNode: JsonNode? = null) {

    fun transfer(from: JsonNode, to: JsonNode) {
        val sourceNode = nodeGetter.extract(from)
        when {
            matcher.match(sourceNode) -> nodeSetter.set(valueNode.deepCopy(), to)
            defaultNode != null -> nodeSetter.set(defaultNode.deepCopy(), to)
            else -> throw CannotConvertValue(from, nodeGetter.path, matcher.toString())
        }
    }
}

internal object JsonValueConverterFactory {
    fun create(settings: JsonNode): JsonValueConverter {

        val sourceGetter = JsonFieldGetterFactory.create(settings.path("sourcePath").asText())
        val targetSetter = JsonFieldSetterFactory.create(settings.path("targetPath").asText())
        val matchNode = settings.get("matchValue")
        val misMatchNode = settings.get("mismatchValue")
        val nodeMatcher = JsonNodeMatcherFactory.create(settings.get("matcher"))
        return JsonValueConverter(sourceGetter, nodeMatcher, targetSetter, matchNode, misMatchNode)
    }
}