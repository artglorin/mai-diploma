package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import java.math.BigDecimal

object MapperFactory {
    fun create(nodeType: String, fieldName: String) = when (nodeType) {
        "array" -> JsonArrayMapper(fieldName)
        "string" -> JsonStringMapper(fieldName)
        "integer" -> JsonIntMapper(fieldName)
        "double" -> JsonBigDecimalMapper(fieldName)
        "object" -> JsonObjectMapper(fieldName)
        "long" -> JsonLongMapper(fieldName)
        else -> CopyMapper()
    }
}

interface JsonMapper<in S : JsonNode, in T : JsonNode> {
    fun map(errors: MutableMap<String, String>, from: S, to: T): Boolean
}

abstract class AbstractJsonMapper<V : Any, in S : JsonNode, in T : JsonNode>(
        private val field: String,
        private val typePredicate: (JsonNode) -> Boolean,
        private val nodePredicate: (JsonNode) -> Boolean,
        private val dataGetter: (JsonNode) -> V,
        private val dataSetter: (V, T) -> Unit

) : JsonMapper<S, T> {
    override fun map(errors: MutableMap<String, String>, from: S, to: T): Boolean {
        val valueNode = from.path(field)
        return if (typePredicate.invoke(valueNode).and(nodePredicate.invoke(valueNode))) {
            dataSetter.invoke(dataGetter.invoke(valueNode), to)
            true
        } else {
            errors[this.toString()] = "$field by type ${valueNode.nodeType} is unfit to be mapping ."
            false
        }
    }
}

//<editor-fold desc="Complex Mappers">
class JsonOrGroupMapper(private val groupName: String, private val mappers: List<JsonMapper<JsonNode, JsonNode>>)
    : JsonMapper<JsonNode, JsonNode> {
    override fun map(errors: MutableMap<String, String>, from: JsonNode, to: JsonNode): Boolean {
        val fakeError = HashMap<String, String>()
        for (mapper in mappers) {

            if (mapper.map(fakeError, from, to)) {
                return true
            }
        }
        errors["orMapper"] = "No one of mapper in group '$groupName' match in object: $from"
        return false
    }

    override fun toString(): String {
        return "JsonOrGroupMapper"
    }
}

class JsonAllGroupMapper(
        private val groupName: String,
        private val mappers: List<JsonMapper<JsonNode, JsonNode>>,
        private val mapIfError: Boolean = false)
    : JsonMapper<JsonNode, JsonNode> {
    override fun map(errors: MutableMap<String, String>, from: JsonNode, to: JsonNode): Boolean {
        val fakeError = HashMap<String, String>()
        val temp = if (mapIfError) to else JsonNodeFactory.instance.objectNode()
        for (mapper in mappers) {
            mapper.map(fakeError, from, temp)
        }
        if (fakeError.isNotEmpty()) {
            errors["orMapper"] = "Some mappers in group '$groupName' finished with errors $fakeError in object: $from"
            errors.putAll(fakeError)
            return false
        } else if (mapIfError.not()) {
            temp.copyTo(to)
        }
        return true
    }

    override fun toString(): String {
        return "JsonAllGroupMapper"
    }
}
//</editor-fold>

//<editor-fold desc="SimpleMappers">
class JsonArrayMapper(
        private val field: String,
        nodePredicate: (JsonNode) -> Boolean = { _ -> true }

) : AbstractJsonMapper<ArrayNode, JsonNode, JsonNode>(
        field,
        { node -> node.isArray },
        nodePredicate,
        { node -> node as ArrayNode },
        { value, to ->
            if (to.isArray) {
                (to as ArrayNode).addAll(value)
            } else if (to.isObject) {
                (to as ObjectNode).putArray(field).addAll(value)
            }
        }
) {
    override fun toString(): String {
        return "JsonArrayMapper for field: '$field'"
    }
}

open class JsonStringMapper(
        private val field: String,
        nodePredicate: (it: JsonNode) -> Boolean = { _ -> true })
    : AbstractJsonMapper<String, JsonNode, ObjectNode>(
        field,
        { node -> node.isTextual },
        nodePredicate,
        { node -> node.textValue() },
        { value, to -> to.put(field, value) }
) {
    override fun toString(): String {
        return "String Mapper for field: $field"
    }
}

open class JsonNotBlankStringMapper(
        private val field: String)
    : JsonStringMapper(
        field,
        { node -> node.asText().isNotBlank() }
) {
    override fun toString(): String {
        return "NotBlank String mapper for field: '$field'"
    }
}

open class JsonIntMapper(
        private val field: String,
        nodePredicate: (it: JsonNode) -> Boolean = { _ -> true })
    : AbstractJsonMapper<Int, JsonNode, ObjectNode>(
        field,
        { node -> node.canConvertToInt() },
        nodePredicate,
        { node -> node.intValue() },
        { value, to -> to.put(field, value) }
) {

    override fun toString(): String {
        return "JsonIntMapper for field: '$field'"
    }
}

open class JsonLongMapper(
        private val field: String,
        nodePredicate: (it: JsonNode) -> Boolean = { _ -> true })
    : AbstractJsonMapper<Long, JsonNode, ObjectNode>(
        field,
        { node -> node.canConvertToLong() },
        nodePredicate,
        { node -> node.longValue() },
        { value, to -> to.put(field, value) }
) {

    override fun toString(): String {
        return "JsonLongMapper for field: '$field'"
    }
}

open class JsonBigDecimalMapper(
        private val field: String,
        nodePredicate: (it: JsonNode) -> Boolean = { _ -> true })
    : AbstractJsonMapper<BigDecimal, JsonNode, ObjectNode>(
        field,
        { node -> node.isBigDecimal },
        nodePredicate,
        { node -> node.decimalValue() },
        { value, to -> to.put(field, value) }
) {

    override fun toString(): String {
        return "JsonBigDecimalMapper for field: '$field'"
    }
}

fun JsonNode.copyTo(other: JsonNode) {
    if (isObject && other.isObject) {
        other as ObjectNode
        fields().forEach {
            other.set(it.key, it.value)
        }
    } else if (isArray && other.isArray) {
        other as ArrayNode
        this as ArrayNode
        other.addAll(this)
    }
}

class CopyMapper : JsonMapper<JsonNode, JsonNode> {
    override fun map(errors: MutableMap<String, String>, from: JsonNode, to: JsonNode): Boolean {
        from.copyTo(to)
        return true
    }

    override fun toString(): String {
        return "CopyMapper"
    }
}

class JsonObjectMapper(
        private val field: String,
        private val fieldsMapper: JsonMapper<JsonNode, JsonNode> = JsonAllGroupMapper("field", listOf(CopyMapper() as JsonMapper<JsonNode, JsonNode>)))
    : JsonMapper<JsonNode, ObjectNode> {
    override fun map(errors: MutableMap<String, String>, from: JsonNode, to: ObjectNode): Boolean {
        val valueNode = from.path(field)
        val innerObject = JsonNodeFactory.instance.objectNode()
        return if (valueNode.isObject.and(fieldsMapper.map(errors, valueNode, innerObject))) {
            to.set(field, innerObject)
            true
        } else {
            errors[field] = "$field by type ${valueNode.nodeType} is unfit to be mapping ${this} mapper."
            false
        }
    }

    override fun toString(): String {
        return "JsonObjectMapper for field: '$field'"
    }
}

class JsonEnumMapper(
        private val field: String,
        vararg enumText: String)
    : JsonStringMapper(
        field,
        {it ->
            val fieldValue = it.asText()
            enumText.all { it.equals(fieldValue, true) }
        }) {

    override fun toString(): String {
        return "JsonEnumMapper for field: '$field'"
    }
}
//</editor-fold>