package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import java.math.BigDecimal

object JacksonNodeFactory {
    private val inputCheck = Regex("(?<primitive>(integer|long|string|double|object|array))\\s*?(?:\\((?<args>([^)]+))?)\\)")
    private val factory = JsonNodeFactory.instance
    private val mapper = AppJsonMappers.ignoreUnknown

    fun <T: Any>createModuleResult(moduleId: String, series: String, data: T): JsonNode {
        return factory.objectNode().apply {
            put("moduleId", moduleId)
            put("series", series)
            putPOJO("data", data)
        }
    }

    fun create(nodeDescription: String): JsonNode {
        val matchResult = inputCheck.matchEntire(nodeDescription)
                ?: throw JsonException("Specified wrong type: $nodeDescription, Must be on of pattern:[], {}, integer\\(\\d+\\)\$, long\\(\\d+\\)\$, double\\(\\d+\\.\\d+\\)\$, string\\([^)]\\)\$")
        val primitive = matchResult.groups["primitive"]!!.value
        val args = matchResult.groups["args"]?.value
        return try {
            when (primitive) {
                "object" -> factory.objectNode()
                "array" -> factory.arrayNode()
                "integer" -> factory.numberNode(args?.toInt() ?: 0)
                "double" -> factory.numberNode(args?.toBigDecimal() ?: BigDecimal.ZERO)
                "long" -> factory.numberNode(args?.toLong() ?: 0L)
                "string" -> factory.textNode(args?:"")
                else -> throw JsonException("Cannot create node")
            }
        } catch (e: NumberFormatException) {
            throw JsonException("Cannot create node due illegal number format: $e")
        }
    }
    fun create(type: ContainerType) =
            when (type) {
                is ArrayType -> factory.arrayNode()
                is ObjectType -> factory.objectNode()
                else -> throw IllegalStateException("Cannot create type $type")
            }!!

    fun toJson(item: Any): JsonNode = mapper.valueToTree(item)
}