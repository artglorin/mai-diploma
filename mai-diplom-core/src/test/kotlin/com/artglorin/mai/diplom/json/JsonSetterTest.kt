package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 06/05/2018
 */
internal class JsonSetterTest {

    @TestFactory
    fun `test Node$createPath`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val path: String,
                val target: JsonNode,
                val expectTarget: JsonNode,
                val value: JsonNode,
                val replace: Boolean = false
        )

        val factory = JsonNodeFactory.instance
        return arrayOf(

                TestData(
                        "Add to emptyNode: path",
                        "path",
                        factory.objectNode(),
                        factory.objectNode().put("path", 1),
                        factory.numberNode(1)
                ),
                TestData(
                        "Add to Array: [2]",
                        "[2]",
                        factory.arrayNode(),
                        factory.arrayNode().fillTo(1).add(1),
                        factory.numberNode(1)
                ),
                TestData(
                        "Add to emptyNode: path.integer",
                        "path.integer",
                        factory.objectNode(),
                        factory.objectNode().apply {
                            putObject("path").apply {
                                put("integer", 1)
                            }
                        },
                        factory.numberNode(1)
                ),
                TestData(
                        "Add to empty node: array[0]",
                        "array[0]",
                        factory.objectNode(),
                        factory.objectNode().apply {
                            putArray("array").apply {
                                add(1)
                            }
                        },
                        factory.numberNode(1)
                ),
                TestData(
                        "Add to node with exist array: array[0]",
                        "array[0]",
                        factory.objectNode().apply { putArray("array") },
                        factory.objectNode().apply {
                            putArray("array").apply {
                                add(1)
                            }
                        },
                        factory.numberNode(1)
                ),
                TestData(
                        "Add to node with exist Object, replace=true: array[0]",
                        "array[0]",
                        factory.objectNode().apply { putObject("array") },
                        factory.objectNode().apply {
                            putArray("array").apply {
                                add(1)
                            }
                        },
                        factory.numberNode(1),
                        true
                ),
                TestData(
                        "Given: Source has array with same name and has values." +
                                " Expect: setter must not erase exist values",
                        "array[0]",
                        factory.objectNode().apply { putArray("array").fillTo(1).set(1, factory.numberNode(2)) },
                        factory.objectNode().apply {
                            putArray("array").apply {
                                add(1)
                                add(2)
                            }
                        },
                        factory.numberNode(1),
                        true
                ),
                TestData(
                        "Given: Source has object with same name and has different fields." +
                                " Expect: setter must not erase exist fields",
                        "obj.integer",
                        factory.objectNode().apply {
                            putObject("obj").apply {
                                put("one", 1)
                                put("two", 2)
                            }
                        },
                        factory.objectNode().apply {
                            putObject("obj").apply {
                                put("one", 1)
                                put("two", 2)
                                put("integer", 3)
                            }
                        },
                        factory.numberNode(3),
                        true
                ),
                TestData(
                        "Path: array[5].item.value={}",
                        "array[5].item[0].value=object()",
                        factory.objectNode().apply { },
                        factory.objectNode().apply {
                            putArray("array").apply {
                                add(nullNode())
                                add(nullNode())
                                add(nullNode())
                                add(nullNode())
                                add(nullNode())
                                add(objectNode().apply {
                                    putArray("item").apply {
                                        add(objectNode().apply {
                                            putObject("value")
                                        })
                                    }
                                }
                                )
                            }
                        },
                        factory.objectNode(),
                        true
                )
        ).map {
            dynamicTest("Test create path on JsonNode. Case: ${it.caseName}") {
                JsonValueSetter.setValue(it.target, it.path, it.value, it.replace)
                assertEquals(it.expectTarget, it.target)
            }
        }
    }

    @TestFactory
    fun `test throw JsonPathCreationException is throws in Node$createPath`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val path: String,
                val value: JsonNode,
                val target: JsonNode
        )

        val factory = JsonNodeFactory.instance
        return arrayOf(
                TestData("Given: Object has a filed by name 'item'. Setter 'item'." +
                        " Expect: Field cannot be erased",
                        "path",
                        factory.numberNode(1),
                        factory.objectNode().apply {
                            putArray("path")
                        }
                ),
                TestData("Given: Array has an object by index 0. Setter '[0]'." +
                        " Expect: Field cannot be erased",
                        "[0]",
                        factory.numberNode(2),
                        factory.arrayNode().addObject()
                )
        ).map {
            dynamicTest("test throw JsonCreationException${it.caseName}") {
                assertThrows(JsonPathCreationException::class.java, {
                    JsonValueSetter.setValue(it.target, it.path, it.value)
                })
            }
        }
    }



}