package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun ArrayNode.fillTo(size: Int): ArrayNode {
    while (this.size() < size + 1) {
        add(nullNode())
    }
    return this
}

/**
 * Jackson mappers for use in an Application
 */
object AppJsonMappers  {
    /**
     * Mapper which ignore unknown properties while deserialization process
     */
    val ignoreUnknown: ObjectMapper = {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper
    }.invoke()
}

/**
 * Check this by present value
 */
fun JsonNode.isExist() = isContainerNode.or(isValueNode)

/**
 * Check this node is an Object and has elements
 */
fun JsonNode.isNotEmptyObject() = isObject.and(size() > 0)

/**
 * Check this node is an Object and has no elements
 */
fun JsonNode.isEmptyObject() = isObject.and(size() == 0)

/**
 * Copy all data from this node to other. Is this node is an ObjectNode or is an ArrayNode
 * @return this node
 */
fun JsonNode.copyTo(other: JsonNode):JsonNode {
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
    return this
}