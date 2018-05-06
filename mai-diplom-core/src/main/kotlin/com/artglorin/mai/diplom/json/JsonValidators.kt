package com.artglorin.mai.diplom.json

import com.artglorin.mai.diplom.toLowerCaseCollection
import com.fasterxml.jackson.databind.JsonNode

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
        private val types = JsonType.ARRAY.toLowerCaseCollection()
    }


}

interface JsonValidator<in T : JsonNode> {
    fun validate(errors: MutableMap<String, String>, node: T): Boolean
}

enum class JsonType {
    STRING, OBJECT, ARRAY, INTEGER, DOUBLE, LONG
}