package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal
import java.math.BigInteger

sealed class JsonNodeMatcher {
    abstract fun match(node: JsonNode) : Boolean
}

class StringEqualsMatcher(private val string: String): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node.textValue()
    }

    override fun toString() = "StringEqualsMatcher math by $string"
}

class IntegerEqualsMatcher(private val string: Int): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node.intValue()
    }
}

class BigIntegerEqualsMatcher(private val template: BigInteger): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return template == node.bigIntegerValue()
    }
}

class DoubleEqualsMatcher(private val string: Double): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node.doubleValue()
    }
}

class BigDecimalEqualsMatcher(private val string: BigDecimal): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node.decimalValue()
    }
}

class LongEqualsMatcher(private val string: Long): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node.longValue()
    }
}

class ArrayEqualsMatcher(private val string: ArrayNode): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node
    }
}

class ObjectEqualsMatcher(private val string: ObjectNode): JsonNodeMatcher() {
    override fun match(node: JsonNode): Boolean {
        return string == node
    }
}

object JsonNodeMatcherFactory {
    private val types: Collection<String> = JsonType.values().map { it.name }
    /**
     *
     * Required structure
     * general:
     * {
     *  "name" :  {
     *      "type" : "enum",
     *      "values" : ["equals"]
     *  },
     *  "settings": {
     *      "description" : "specified settings for each type",
     *      "type" : "object"
     *  }
     * }
     * for equals type:
     * {
     *      "type" {
     *          "type" : "eum",
     *          "values" :  ["string", "integer", "bigInteger", "long", "double", "bigDecimal", "array", "object"]
     *      },
     *      "template" : {
     *          "description" : "any object which correspond to type value",
     *          "type" : "object"
     *      }
     * }
     */
    fun create(settings: JsonNode): JsonNodeMatcher {
        if (settings.isObject.not()) {
            throw IllegalStateException("Cannot create TransformerMatcher from not an ObjectNode. Current: ${settings.nodeType}")
        }
        val name = settings.path("name").textValue() ?: throw IllegalStateException("'name' field must not be null")
        return when (name) {
            "equals" -> {
                val template = settings.get("template")
                when {
                    template.isTextual -> StringEqualsMatcher(template.asText())
                    template.isInt -> IntegerEqualsMatcher(template.asInt())
                    template.isBigInteger -> BigIntegerEqualsMatcher(template.bigIntegerValue())
                    template.isLong -> LongEqualsMatcher(template.asLong())
                    template.isDouble -> DoubleEqualsMatcher(template.asDouble())
                    template.isBigDecimal -> BigDecimalEqualsMatcher(template.decimalValue())
                    template.isArray -> ArrayEqualsMatcher(template as ArrayNode)
                    template.isObject -> ObjectEqualsMatcher(template as ObjectNode)
                    else -> throw IllegalArgumentException("Unsupported json type: ${template.nodeType}. Supported values: $types")
                }
            }
//            "lessThen" -> LessThenTransformerMatcher(settings.path("fromValue"))
//            "greatThen" -> {
//                val template = settings.get("template")
//                when {
//                    template.isTextual -> StringEqualsMatcher(template.asText())
//                    template.isInt -> IntegerEqualsMatcher(template.asInt())
//                    template.isBigInteger -> BigIntegerEqualsMatcher(template.bigIntegerValue())
//                    template.isLong -> LongEqualsMatcher(template.asLong())
//                    template.isDouble -> DoubleEqualsMatcher(template.asDouble())
//                    template.isBigDecimal -> BigDecimalEqualsMatcher(template.decimalValue())
//                    template.isArray -> ArrayEqualsMatcher(template as ArrayNode)
//                    template.isObject -> ObjectEqualsMatcher(template as ObjectNode)
//                    else -> throw IllegalArgumentException("Unsupported json type: ${template.nodeType}. Supported values: $types")
//                }
//            }
//            "between" -> BetweenTransformerMatcher(settings.path("from"), settings.path("to"))
            else -> throw IllegalArgumentException("Unsupported TransformerMatcher type: '$name'")
        }
    }
}