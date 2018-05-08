package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JsonTransferMapperTest {

    @TestFactory
    fun `test JsonTransferMapper`(): Collection<DynamicTest> {
        data class TestData(
                val mappers: List<TransferValueItem>,
                val from: JsonNode,
                val to: JsonNode,
                val expect: JsonNode
        )

        val factory = JsonNodeFactory.instance
        return listOf(
                TestData(
                        listOf(
                                TransferValueItem("obj.string", "obj.string"),
                                TransferValueItem("string", "string"),
                                TransferValueItem("integer", "integer")
                        ),
                        factory.objectNode().apply {
                            putObject("obj").apply {
                                put("string", "in object")
                            }
                            put("string", "in root")
                            put("integer", 1)
                        },
                        factory.objectNode(),
                        factory.objectNode().apply {
                            putObject("obj").apply {
                                put("string", "in object")
                            }
                            put("string", "in root")
                            put("integer", 1)
                        }
                ),
                TestData(
                        listOf(
                                TransferValueItem("obj.obj.array[1].it", "string"),
                                TransferValueItem("string", "map"),
                                TransferValueItem("integer", "integer")
                        ),
                        factory.objectNode().apply {
                            putObject("obj").apply {
                                put("string", "in object")
                                putObject("obj").putArray("array").fillTo(0).addObject().put("it", "string")
                            }
                            put("string", "in root")
                            put("integer", 1)
                        },
                        factory.objectNode().apply {
                            put("one", "one")
                            put("two", 2)
                        },
                        factory.objectNode().apply {
                            put("one", "one")
                            put("two", 2)
//                            putObject("obj").apply {
//                                put("string", "in object")
//                                putObject("obj").putArray("array").fillTo(0).addObject().put("it", "string")
//                            }
                            put("map", "in root")
                            put("string", "string")
                            put("integer", 1)
                        }
                )
        ).map {
            dynamicTest("Test transfer mapping") {
                assertTrue(JsonTransferMapper(it.mappers).map(HashMap(), it.from, it.to))
                assertEquals(it.expect, it.to)
            }
        }
    }
}