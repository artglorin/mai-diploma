package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

private sealed class FieldSetter {
    var target: JsonNode? = null
    var source: JsonNode? = null
        get() = if (field == null) createContainer() else field

    abstract fun set(source: JsonNode, target: JsonNode): JsonNode
    fun set() {
        set(requireNotNull(source), requireNotNull(target))
    }

    abstract fun createContainer(): JsonNode

    fun join(setter: FieldSetter) {

        val node = set(requireNotNull(setter.source), requireNotNull(target))
        if (setter.target == null) {
            setter.target = node
        }
    }
}

private class ArraySetter(private val index: Int, private val replaceExist: Boolean) : FieldSetter() {

    override fun createContainer(): JsonNode = JacksonNodeFactory.create(ArrayType)

    override fun set(source: JsonNode, target: JsonNode): JsonNode {
        if (target.isArray.not()) throw JsonPathCreationException("Cannot add value to not ArrayNode by index $index to target: ${target.nodeType}")
        target as ArrayNode
        if (target.size() > index) {
            val path = target.get(index)
            if (path.isExist()) {
                if ((path.nodeType == source.nodeType).not().and(replaceExist.not())) {
                    throw JsonPathCreationException("Cannot add item to ArrayNode due filed is exist")
                }
                if (JsonType.isContainer(path)) {
                    source.copyTo(path)
                    return path
                }
            }
        } else {
            target.fillTo(index)
        }
        target.set(index, source)
        return source
    }
}

private class ObjectSetter(private val fieldName: String, private val replaceExist: Boolean) : FieldSetter() {

    override fun createContainer(): JsonNode = JacksonNodeFactory.create(ObjectType)

    override fun set(source: JsonNode, target: JsonNode): JsonNode {
        if (target.isObject.not()) throw JsonPathCreationException("Cannot add item to not an ObjectNode. target: ${target.nodeType}")
        val path = target.path(fieldName)
        if (path.isExist()) {
            if ((path.nodeType == source.nodeType).not().and(replaceExist.not())) {
                throw JsonPathCreationException("Cannot add item to ObjectNode due filed is exist")
            }
            if (JsonType.isContainer(path)) {
                if (path.nodeType == source.nodeType) {
                    source.copyTo(path)
                    return path
                }
            }
        }
        target as ObjectNode
        target.set(fieldName, source)
        return source
    }
}

private class SetterFactory {
    companion object {
        private val indexMatcher = Regex(".*?\\[(\\d+?)]")

        fun create(name: String, replaceExist: Boolean): List<FieldSetter> {
            val fieldName: String
            val index: Int?
            val matchResult = indexMatcher.matchEntire(name)
            return if (matchResult != null) {
                index = matchResult.groups[1]?.value?.toInt()
                fieldName = name.substringBeforeLast("[")
                return if (fieldName.isNotBlank()) {
                    if (index != null) {
                        listOf(ObjectSetter(fieldName, replaceExist), ArraySetter(index, replaceExist))
                    } else {
                        listOf(ObjectSetter(fieldName, replaceExist))
                    }
                } else if (index != null) {
                    listOf(ArraySetter(index, replaceExist))
                } else emptyList()
            } else {
                listOf(ObjectSetter(name, replaceExist))
            }
        }
    }

}

object JsonValueSetter {

    fun setValue(target: JsonNode, fullPath: String, value: JsonNode,  replaceExist: Boolean = false) {
        if (target.isContainerNode.not()) {
            throw JsonPathCreationException("Cannot crate path on not container node")
        }
        JsonPathValidator.validate(fullPath)
        val pathWithoutType = fullPath.substringBefore("=")
        val fieldSetters = pathWithoutType.split(".").filter { it.isNotBlank() }
                .flatMap { SetterFactory.create(it, replaceExist) }
        var setterPointer = fieldSetters.first()
        setterPointer.target = target
        for (setter in fieldSetters.drop(1)) {
            setterPointer.join(setter)
            setterPointer = setter
        }
        setterPointer.source = value
        setterPointer.set()
    }
}