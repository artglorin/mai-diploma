package com.artglorin.mai.diplom.json

import com.artglorin.mai.diplom.core.ConverterDescription
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode

interface JsonValueConverter {
    fun transfer(from: JsonNode, to: JsonNode): Boolean
}

class SingleJsonValueConverter(private val nodeGetter: JsonNodeGetter,
                               private val matcher: JsonNodeMatcher,
                               private val nodeSetter: JsonNodeSetter,
                               private val valueNode: JsonNode,
                               private val defaultNode: JsonNode = MissingNode.getInstance(),
                               private val required: Boolean = false) : JsonValueConverter {

    override fun transfer(from: JsonNode, to: JsonNode): Boolean {
        val sourceNode = nodeGetter.extract(from)
        return when {
            matcher.match(sourceNode) -> {
                nodeSetter.set(valueNode.deepCopy(), to); return true
            }
            defaultNode.isMissingNode.not()-> {
                nodeSetter.set(defaultNode.deepCopy(), to); return true
            }
            else -> if (required) throw CannotConvertValue(from, nodeGetter.path, matcher.toString()) else false
        }
    }
}

class CopyJsonValueConverter(private val nodeGetter: JsonNodeGetter,
                               private val nodeSetter: JsonNodeSetter) : JsonValueConverter {

    override fun transfer(from: JsonNode, to: JsonNode): Boolean {
        nodeSetter.set(nodeGetter.extract(from), to)
        return true
    }
}

class ComplexJsonValueConverters(private val others: Collection<JsonValueConverter>) : JsonValueConverter {
    override fun transfer(from: JsonNode, to: JsonNode): Boolean {
        return others.any { it.transfer(from, to) }
    }
}

internal object JsonValueConverterFactory {
    fun create(settings: ConverterDescription): JsonValueConverter {

        val sourceGetter = JsonNodeGetterFactory.create(settings.sourcePath)
        val targetSetter = JsonNodeSetterFactory.create(settings.targetPath)
        return when {
            "complex" == settings.matcherId -> ComplexJsonValueConverters((settings.matcherSettings as ArrayNode)
                    .map {
                        val matcher = JsonNodeMatcherFactory.create(it)
                        val matchValue = it.get("matchValue")
                        SingleJsonValueConverter(sourceGetter, matcher, targetSetter, matchValue
                                ?: settings.matchValue, settings.mismatchValue)
                    })
            "copy" == settings.matcherId -> return CopyJsonValueConverter(sourceGetter, targetSetter)
            else -> {
                val nodeMatcher = JsonNodeMatcherFactory.create(settings.matcherSettings)
                val matchValue = settings.matcherSettings.get("matchValue")
                SingleJsonValueConverter(sourceGetter, nodeMatcher, targetSetter, matchValue ?: settings.matchValue, settings.mismatchValue)
            }
        }
    }
}