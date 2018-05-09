package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

object JsonValueSetter {

    fun setValue(target: JsonNode, fullPath: String, value: JsonNode, replaceExist: Boolean = false) {
        if (target.isContainerNode.not()) {
            throw JsonPathCreationException("Cannot crate path on not container node")
        }
        JsonFieldSetterFactory.create(fullPath, replaceExist).set(value, target)
    }
}

class JsonFieldSetterFactory {
    companion object {
        private val indexMatcher = Regex(".*?\\[(\\d+?)]")

        fun create(name: String, replaceExist: Boolean = false): JsonFieldSetter {
            JsonPathValidator.validate(name)
            val parts = name.split(".").filter { it.isNotBlank() }
            val setters = ArrayList<ContainerFiledSetter>()
            for (part in parts) {
                val fieldName: String
                val index: Int?
                val matchResult = indexMatcher.matchEntire(part)
                if (matchResult != null) {
                    index = matchResult.groups[1]?.value?.toInt()
                    fieldName = part.substringBeforeLast("[")
                    if (fieldName.isNotBlank()) {
                        setters.add(ObjectSetter(fieldName, replaceExist))
                        if (index != null) {
                            setters.add(ArraySetter(index, replaceExist))
                        }
                    } else if (index != null) {
                        setters.add(ArraySetter(index, replaceExist))
                    }
                } else {
                    setters.add(ObjectSetter(part, replaceExist))
                }
            }
            return if (setters.size == 1) setters.first() else ComplexSetter(setters)
        }
    }

}

sealed class JsonFieldSetter {
    var target: JsonNode? = null
    open var source: JsonNode? = null

    abstract fun set(source: JsonNode, target: JsonNode): JsonNode

    fun set() {
        set(requireNotNull(source), requireNotNull(target))
    }

}

private abstract class ContainerFiledSetter : JsonFieldSetter() {
    override var source: JsonNode? = null
    abstract fun createContainer(): JsonNode
}

private fun ContainerFiledSetter.join(setterJson: ContainerFiledSetter) {
    val node = set(requireNotNull(setterJson.source?: setterJson.createContainer()), requireNotNull(target))
    setterJson.target = node
}

private class ArraySetter(private val index: Int, private val replaceExist: Boolean) : ContainerFiledSetter()  {

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

private class ObjectSetter(private val fieldName: String, private val replaceExist: Boolean) : ContainerFiledSetter() {

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

private class ComplexSetter(private val setters: List<ContainerFiledSetter>) : JsonFieldSetter() {

    override fun set(source: JsonNode, target: JsonNode): JsonNode {
         var setterPointer = setters.first()
        setterPointer.target = target
        for (setter in setters.drop(1)) {
            setterPointer.join(setter)
            setterPointer = setter
        }
        setterPointer.source = source
        setterPointer.set()
        setters.forEach { it.target = null ; it.source = null }
        return source
    }

}
