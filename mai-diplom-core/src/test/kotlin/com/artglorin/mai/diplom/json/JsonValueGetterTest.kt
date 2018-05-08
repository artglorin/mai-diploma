package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JsonValueGetterTest {

    @TestFactory
    fun `test JsonValueGetter`():Collection<DynamicTest> {
        data class TestData (
                val caeName: String,
                val path: String,
                val target: JsonNode,
                val result: JsonNode
        )

        val factory = JsonNodeFactory.instance
        return listOf(
            TestData(
                    "Given Object with value. Expect result is returned",
                    "it",
                    factory.objectNode().put("it", 1),
                    factory.numberNode(1)

            ),
            TestData(
                    "Given Array with value. Expect result returned",
                    "[0]",
                    factory.arrayNode().add(1),
                    factory.numberNode(1)

            ),
            TestData(
                    "Given Array without value. Expect MissingNode",
                    "[0]",
                    factory.arrayNode(),
                    factory.numberNode(1).path("3")
            ),
            TestData(
                    "Given {'it':[{'string':'target'}]}, path=it[0].string . Expect result returned",
                    "it[0].string",
                    factory.objectNode().apply {
                        putArray("it").addObject().put("string","target")
                    },
                    factory.textNode("target")

            ),
            TestData(
                    "Given Array without value. Expect MissingNode",
                    "[0]",
                    factory.arrayNode(),
                    factory.numberNode(1).path("3")
            ),
            TestData(
                    "Given Object without value. Expect MissingNode",
                    "it",
                    factory.objectNode(),
                    factory.objectNode().path("it")

            )
        ).map {
            dynamicTest(it.caeName){
                assertEquals(it.result, JsonValueGetter.get(it.path, it.target))
            }
        }
    }
}