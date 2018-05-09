package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

object JsonValueGetter {
    fun get(pathToValue: String, source: JsonNode): JsonNode {
        return JsonFieldGetterFactory.create(pathToValue).extract(source)
    }
}

class JsonFieldGetterFactory {
    companion object {
        private val indexMatcher = Regex(".*?\\[(\\d+?)]")

        fun create(name: String): JsonFieldGetter {
            return CompositeGetter(name.split(".").map(::simpleCreate))
        }

        private fun simpleCreate(name: String): JsonFieldGetter {
            val fieldName: String
            val index: Int?
            val matchResult = indexMatcher.matchEntire(name)
            return if (matchResult != null) {
                index = matchResult.groups[1]?.value?.toInt()
                fieldName = name.substringBeforeLast("[")
                return if (fieldName.isNotBlank()) {
                    if (index != null) {
                        CompositeGetter(listOf(ObjectGetter(fieldName), ArrayGetter(index)))
                    } else {
                        ObjectGetter(fieldName)
                    }
                } else if (index != null) {
                    ArrayGetter(index)
                } else MissGetter()
            } else {
                ObjectGetter(name)
            }
        }
    }

}

sealed class JsonFieldGetter {
    abstract fun extract(from: JsonNode): JsonNode
}

private class ArrayGetter(private val index: Int): JsonFieldGetter() {
    override fun extract(from: JsonNode): JsonNode {
        if(from.isArray.not()) return MissingNode.missingNode
        from as ArrayNode
        return if (from.size() < index -1){
            from.path(index)
        } else {
            from.get(index)?: MissingNode.missingNode
        }
    }
}

private class ObjectGetter(private val name: String): JsonFieldGetter() {
    override fun extract(from: JsonNode): JsonNode {
        if(from.isObject.not()) return MissingNode.missingNode
        from as ObjectNode
        return from.path(name)
    }
}

private class CompositeGetter(var getters: List<JsonFieldGetter>) :JsonFieldGetter(){

    override fun extract(from: JsonNode): JsonNode {
        var result: JsonNode = getters.first().extract(from)
        for (getter in getters.drop(1)) {
            result = getter.extract(result)
        }
        return result
    }

}

private class MissGetter: JsonFieldGetter() {
    override fun extract(from: JsonNode) = MissingNode.missingNode
}

private object MissingNode {
    val missingNode = JsonNodeFactory.instance.objectNode().path("1")!!
}