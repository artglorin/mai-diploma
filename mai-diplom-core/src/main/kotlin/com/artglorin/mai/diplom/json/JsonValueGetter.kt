package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode

object JsonValueGetter {
    fun get(pathToValue: String, source: JsonNode): JsonNode {
        JsonPathValidator.validate(pathToValue)
        val paths = pathToValue.split(".")
        var result = get(GetterFactory.create(paths.first()), source)
        for (path in paths.drop(1) ) {
            if(result.isExist().not()) break
            result = get(GetterFactory.create(path), result)
        }
        return result
    }
}
private object MissingNode {
    val missingNode = JsonNodeFactory.instance.objectNode().path("1")!!
}
private sealed class JsonGetter {
    abstract fun extract(from: JsonNode): JsonNode
}

private class ArrayGetter(private val index: Int): JsonGetter() {
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

private fun get(getters: List<JsonGetter>, source: JsonNode): JsonNode {
    var result: JsonNode = getters.first().extract(source)
    for (getter in getters.drop(1)) {
        result = getter.extract(result)
    }
    return result
}

private class ObjectGetter(private val name: String): JsonGetter() {
    override fun extract(from: JsonNode): JsonNode {
        if(from.isObject.not()) return MissingNode.missingNode
        from as ObjectNode
        return from.path(name)
    }
}

private class GetterFactory {
    companion object {
        private val indexMatcher = Regex(".*?\\[(\\d+?)]")

        fun create(name: String): List<JsonGetter> {
            val fieldName: String
            val index: Int?
            val matchResult = indexMatcher.matchEntire(name)
            return if (matchResult != null) {
                index = matchResult.groups[1]?.value?.toInt()
                fieldName = name.substringBeforeLast("[")
                return if (fieldName.isNotBlank()) {
                    if (index != null) {
                        listOf(ObjectGetter(fieldName), ArrayGetter(index))
                    } else {
                        listOf(ObjectGetter(fieldName))
                    }
                } else if (index != null) {
                    listOf(ArrayGetter(index))
                } else emptyList()
            } else {
                listOf(ObjectGetter(name))
            }
        }
    }

}
