package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 14/05/2018
 */
internal class JsonNodeMatcherFactoryTest {

    /**
     * {
     *  "name" :  "equals",
     *  "settings": {
     *      "type" : ["string", "integer", "bigInteger", "long", "double", "bigDecimal", "array", "object"],
     *      "template" : ["string", "integer", "bigInteger", "long", "double", "bigDecimal", "array", "object"]
     *  }
     * }
     */
    @TestFactory
    fun `test Equals matcher`(): Collection<DynamicTest> {
        data class TestData(val caseName: String,
                            val settings: JsonNode,
                            val testedNode: JsonNode,
                            val expectedResult: Boolean)

        val factory = JsonNodeFactory.instance
        return listOf(
                TestData("test equals by string. Template: 'hello', target: 'hello', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "string")
                            put("template", "hello")
                        },
                        factory.textNode("hello"),
                        true
                ),
                TestData("test equals by string. Template: 'hello', target: 'Hello', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "string")
                            put("template", "hello")
                        },
                        factory.textNode("Hello"),
                        false
                ),
                TestData("test equals by integer. Template: '1', target: '1', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "integer")
                            put("template", "1")
                        },
                        factory.numberNode(1),
                        true
                ),
                TestData("test equals by integer. Template: '1', target: '2', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "integer")
                            put("template", "1")
                        },
                        factory.numberNode(2),
                        false
                ),
                TestData("test equals by double. Template: '1.0', target: '1.0', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "double")
                            put("template", "1.0")
                        },
                        factory.numberNode(1.0),
                        true
                ),
                TestData("test equals by double. Template: '1.00', target: '0.0', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "double")
                            put("template", "1.00")
                        },
                        factory.numberNode(0.0),
                        false
                ),
                TestData("test equals by long. Template: '1', target: '1L', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "long")
                            put("template", "1")
                        },
                        factory.numberNode(1L),
                        true
                ),
                TestData("test equals by long. Template: '1L', target: '-1L', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "long")
                            put("template", 1L)
                        },
                        factory.numberNode(-1L),
                        false
                ),
                TestData("test equals by bigInteger. Template: '100000000000000000000001000', target: '100000000000000000000001000', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "bigInteger")
                            put("template", BigInteger("100000000000000000000001000"))
                        },
                        factory.numberNode(BigInteger("100000000000000000000001000")),
                        true
                ),
                TestData("test equals by bigInteger. Template: '10000000000000000000000100', target: '-10000000000000000000000100', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "bigInteger")
                            put("template", BigInteger("10000000000000000000000100"))
                        },
                        factory.numberNode(BigInteger("-10000000000000000000000100")),
                        false
                ),
                TestData("test equals by bigDecimal. Template: '10000000000000000000000.1000', target: '10000000000000000000000.1000', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "bigDecimal")
                            put("template", BigDecimal("10000000000000000000000.1000"))
                        },
                        factory.numberNode(BigDecimal("10000000000000000000000.1000")),
                        true
                ),
                TestData("test equals by bigInteger. Template: '10000000000000000000000.100', target: '-10000000000000000000000.100', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "bigDecimal")
                            put("template", BigDecimal("10000000000000000000000.100"))
                        },
                        factory.numberNode(BigDecimal("-10000000000000000000000.100")),
                        false
                ),
                TestData("test equals by array. Template: '[1, 'ttt', 22]', target: '[1, 'ttt', 222]', expected: true",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "array")
                            putArray("template").apply {
                                add(1)
                                add("ttt")
                                add(22)
                            }
                        },
                        factory.arrayNode().apply {
                            add(1)
                            add("ttt")
                            add(22)
                        },
                        true
                ),
                TestData("test equals by array. Template: '[1, 'ttt', 22]', target: '[1, 'tt', 222]', expected: false",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "array")
                            putArray("template").apply {
                                add(1)
                                add("ttt")
                                add(22)
                            }
                        },
                        factory.arrayNode().apply {
                            add(1)
                            add("tt")
                            add(22)
                        }, false
                ),
                TestData("""test equals by object. Template: '{"t" : 2, "two":"two", "ar":[1,2], "obj":{"ww":"@@"}}', target: '{"t" : 2, "two":"two", "ar":[1,2], "obj":{"ww":"@@"}}', expected: true""",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "object")
                            putObject("template").apply {
                                put("t", 2)
                                put("two", "two")
                                putArray("ar").apply {
                                    add(1)
                                    add(2)
                                }
                                putObject("obj").apply {
                                    put("ww","@@")
                                }
                            }
                        },
                        factory.objectNode().apply {
                            put("t", 2)
                            put("two", "two")
                            putArray("ar").apply {
                                add(1)
                                add(2)
                            }
                            putObject("obj").apply {
                                put("ww", "@@")
                            }
                        },
                        true
                ),
                TestData("""test equals by object. Template: '{"t" : 2, "two":"two", "ar":[1,2], "obj":{"ww":"@@"}}', target: '{"t" : 2, "two":"two", "ar":[1,2], "obj":{}}', expected: true""",
                        factory.objectNode().apply {
                            put("name", "equals")
                            put("type", "object")
                            putObject("template").apply {
                                put("t", 2)
                                put("two", "two")
                                putArray("ar").apply {
                                    add(1)
                                    add(2)
                                }
                                putObject("obj").apply {
                                    put("ww","@@")
                                }
                            }
                        },
                        factory.objectNode().apply {
                            put("t", 2)
                            put("two", "two")
                            putArray("ar").apply {
                                add(1)
                                add(2)
                            }
                            putObject("obj")
                        },
                        false
                )
        ).map {
            DynamicTest.dynamicTest(it.caseName, {
                assertEquals(it.expectedResult, JsonNodeMatcherFactory.create(it.settings).match(it.testedNode))
            })
        }
    }
}