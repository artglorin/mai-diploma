package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.MissingNode
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
            return split(name)
        }

        private data class Description (val value: String, val type: ContainerType)

        private fun split(income: String) : JsonFieldGetter {
            val descriptions = ArrayList<Description>()
            for (it in income.split(".").filter { it.isNotBlank() }) {
                val index: String? =  indexMatcher.matchEntire(it)?.groups?.get(1)?.value
                val fieldName: String = if (index != null) it.substringBeforeLast("[") else it
                if (fieldName.isNotBlank()) {
                    descriptions.add(Description(fieldName, ObjectType))
                }
                if (index != null) {
                    descriptions.add(Description(index, ArrayType))
                }
            }
            val result = descriptions.map (::toGetter)
            return if (result.size == 1) result.first() else CompositeGetter(result)
        }

        private fun toGetter(description: Description) : JsonFieldGetter {
            return when (description.type) {
                ArrayType -> ArrayGetter(description.value.toInt())
                ObjectType -> ObjectGetter(description.value)
                else -> { throw IllegalStateException("Unsupported containerType : ${description.type}") }
            }
        }
    }

}

sealed class JsonFieldGetter {
    abstract fun extract(from: JsonNode): JsonNode
}

private class ArrayGetter(private val index: Int): JsonFieldGetter() {
    override fun extract(from: JsonNode): JsonNode {
        if(from.isArray.not()) return MissingNode.getInstance()
        from as ArrayNode
        return if (from.size() < index -1){
            from.path(index)
        } else {
            from.get(index)?: MissingNode.getInstance()
        }
    }
}

private class ObjectGetter(private val name: String): JsonFieldGetter() {
    override fun extract(from: JsonNode): JsonNode {
        if(from.isObject.not()) return MissingNode.getInstance()
        from as ObjectNode
        return from.path(name)
    }
}

private class CompositeGetter(var getters: List<JsonFieldGetter>) :JsonFieldGetter(){

    override fun extract(from: JsonNode): JsonNode {
        var result: JsonNode = from
        for (getter in getters) {
            result = getter.extract(result)
        }
        return result
    }

}