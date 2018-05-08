package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

fun ArrayNode.fillTo(size: Int): ArrayNode {
    while (this.size() < size + 1) {
        add(nullNode())
    }
    return this
}

fun JsonNode.isExist() = isContainerNode.or(isValueNode)

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