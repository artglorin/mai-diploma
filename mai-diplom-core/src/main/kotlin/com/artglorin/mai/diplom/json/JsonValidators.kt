package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

class JsonValidateException(message: String) : RuntimeException(message)
class JsonStringValidator : JsonValidator<JsonNode> {
    override fun validate(errors: MutableMap<String, String>, node: JsonNode) =
            if (node.isTextual.not()) {
                errors["Node '$node' is not a String"]
                false
            } else true
}

class JsonNotBlankStringValidator : JsonValidator<JsonNode> {
    override fun validate(errors: MutableMap<String, String>, node: JsonNode) =
            if (node.isTextual.not() || node.asText().isBlank()) {
                errors["Node $node is not a String or is blank"]
                false
            } else true
}

class JsonTypeValidator : JsonValidator<JsonNode> {
    override fun validate(errors: MutableMap<String, String>, node: JsonNode): Boolean =
            if (node.isTextual && types.contains(node.asText().toLowerCase())) {
                true
            } else {
                errors["JsonTypeValidator.$node"] = "Node is not a TypeConstant"
                false
            }


    companion object {
        private val types = arrayOf(StringType.name, DoubleType.name, IntegerType.name, LongType.name, ArrayType.name, ObjectType.name)
    }


}

interface JsonValidator<in T : JsonNode> {
    fun validate(errors: MutableMap<String, String>, node: T): Boolean
}

sealed class JsonType(val isContainer: Boolean = false, val name: String) {
    fun isPrimitive() = isContainer.not()

    companion object {
        fun values() = arrayOf(StringType, DoubleType, IntegerType, LongType, ArrayType, ObjectType)
        fun isContainer(jacksonType: JsonNode):Boolean = ArrayType.equals(jacksonType).or(ObjectType.equals(jacksonType))

    }

    fun equals(jacksonType: JsonNode): Boolean = when (jacksonType) {
        is IntNode -> this === IntegerType
        is TextNode -> this === StringType
        is DoubleNode -> this === DoubleType
        is LongNode -> this === LongType
        is ArrayNode -> this === ArrayType
        is ObjectNode -> this === ObjectType
        else -> false
    }
}

abstract class PrimitiveType(name: String) : JsonType(false, name)
object StringType : PrimitiveType("string")
object IntegerType : PrimitiveType("integer")
object DoubleType : PrimitiveType("double")
object LongType : PrimitiveType("long")

abstract class ContainerType(name: String) : JsonType(true, name) {
    abstract fun cover(identification: Any, other: JsonNode): JsonNode
}

object ObjectType : ContainerType("object") {

    override fun cover(identification: Any, other: JsonNode): JsonNode {
        return JsonNodeFactory.instance.objectNode().apply {
            set(identification.toString(), other)
        }
    }
}

object ArrayType : ContainerType("array") {
    override fun cover(identification: Any, other: JsonNode): JsonNode {
        val index = identification.toString().toIntOrNull()?: throw JsonException("Cannot cover node to ArrayNode due $identification is not an integer")
        return JsonNodeFactory.instance.arrayNode().fillTo(index).apply {
            set(index, other)
        }
    }
}