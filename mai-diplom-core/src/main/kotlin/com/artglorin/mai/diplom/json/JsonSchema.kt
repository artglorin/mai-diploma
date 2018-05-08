package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.*

/**
 *
 * File with Validations for Json Object according JsonScheme
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class JsonSchemaBuilder {
    var title: String? = null
    var type: JsonType? = null
    private val required = ArrayList<String>()
    private val properties = ArrayList<JsonSchemaProperty>()

    fun newProperty(name: String, type: JsonType) = JsonSchemaProperty(name, type).apply { properties.add(this) }

    fun addRequired(vararg required: String) {
        this.required.addAll(required)
    }

    fun build(): ObjectNode {
        val ttl = title ?: throw IllegalStateException("title must be specified")
        val tp = type ?: throw IllegalStateException("type must be specified")
        if (properties.map { it.name }.containsAll(required).not()) throw IllegalStateException("Required properties must be described")
        val factory = JsonNodeFactory.instance
        return factory.objectNode().apply {
            put("title", ttl)
            put("type", tp.name)
            putArray("required").apply {
                addAll(required.map(factory::textNode))
            }
            if (properties.isNotEmpty()) {
                putObject("properties").apply {
                    properties.forEach {
                        putObject(it.name).apply {
                            put("type", it.type.name)
                            it.description?.apply {
                                put("description", this)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class JsonSchemaProperty(
        val name: String,
        val type: JsonType,
        var description: String? = null)


class JsonSchemaParser private constructor() {

    companion object {
        fun parse(scheme: ObjectNode): Collection<JsonMapper<JsonNode, JsonNode>> {
            val errors: MutableMap<String, String> = HashMap()
            val result = JsonNodeFactory.instance.objectNode()
            JsonNotBlankStringMapper("title").map(errors, scheme, result)
            JsonTypeValidator().validate(errors, scheme.path("type"))
            scheme.path("required").apply {
                when (this) {
                    is ArrayNode -> JsonArrayMapper("required").map(errors, scheme, result)
                    is MissingNode, is NullNode -> {
                    }
                    else -> errors["required"] = "Required field is not an Array"

                }
            }
            val propertyFieldValidator = JsonSchemaPropertyFieldValidator()
            propertyFieldValidator.validate(errors, scheme)
            if (errors.isNotEmpty()) {
                throw JsonValidateException(errors.toString())
            }
            var requiredValidator: JsonMapper<JsonNode, JsonNode>? = null
            if (result.has("required")) {
                val requiredArray = result.get("required") as ArrayNode
                val allFields = requiredArray.map { it.asText() }
                var descriptionMissing = false
                for (field in allFields) {
                    if (propertyFieldValidator.fields.contains(field).not()) {
                        errors["RequiredFieldDescription"] = "Description for required field $field is missing"
                        descriptionMissing = true
                    }
                }
                if (descriptionMissing) {
                    throw JsonValidateException(errors.toString())
                }
                requiredValidator = object : JsonMapper<JsonNode, JsonNode> {
                    override fun map(errors: MutableMap<String, String>, from: JsonNode, to: JsonNode): Boolean {
                        var checkResult = true
                        for (field in allFields) {
                            if (from.has(field).not()) {
                                errors["RequiredField.$field"] = "Required field does not exist in Object"
                                checkResult = false
                            }
                        }
                        return checkResult
                    }

                }
            }

            @Suppress("UNCHECKED_CAST")
            propertyFieldValidator.mappers.add(
                    JsonEnumMapper("title", scheme.get("title").asText()) as JsonMapper<JsonNode, JsonNode>
            )
            requiredValidator?.let { propertyFieldValidator.mappers.add(it) }
            return propertyFieldValidator.mappers
        }
    }
}

class JsonSchemaPropertyFieldValidator : JsonValidator<JsonNode> {
    val fields = ArrayList<String>()
    val mappers = ArrayList<JsonMapper<JsonNode, JsonNode>>()
    private val types = JsonType.values().map { it.name }
    override fun validate(errors: MutableMap<String, String>, node: JsonNode): Boolean {
        val propertiesObject = node.path("properties")
        return when (propertiesObject) {
            is MissingNode -> true
            is ObjectNode -> {
                val localErrors = HashMap<String, String>()
                propertiesObject.fields().forEach {
                    val fieldName = it.key
                    if (it.value.isObject.not()) {
                        errors["properties.$fieldName"] = "It is not object: ${it.value}"
                    } else {
                        val fieldDescription = it.value as ObjectNode
                        val typeNode = fieldDescription.path("type")
                        val nodeType = typeNode.asText().toLowerCase()
                        when {
                            typeNode.isTextual.not() -> errors["properties.$fieldName.type"] = "Node must be string"
                            types.contains(nodeType).not() -> errors["properties.$fieldName.type"] = "Must be on of value: $types"
                            else -> {
                                fields.add(fieldName)
                                @Suppress("UNCHECKED_CAST")
                                mappers.add(MapperFactory.create(nodeType, fieldName) as JsonMapper<JsonNode, JsonNode>)
                            }
                        }
                    }
                }
                errors + localErrors
                return localErrors.isEmpty()
            }
            else -> false
        }

    }
}