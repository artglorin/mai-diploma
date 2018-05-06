package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */
internal class JsonMapperTest {

    private enum class Test {
        ONE
    }

    @TestFactory
    fun `test json String mapper`(): Collection<DynamicTest> {
        data class StringValidateData(
                val caseName: String,
                val path: String,
                val node: JsonNode,
                val mapper: JsonStringMapper,
                val expect: Boolean,
                val expectResult: String)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                StringValidateData(
                        "type = Object, value = 'value'",
                        "test",
                        factory.objectNode().apply {
                            put("test", "value")
                        },
                        JsonStringMapper("test"),
                        true,
                        "value"
                ),
                StringValidateData(
                        "type = Object, value = ''",
                        "test",
                        factory.objectNode().apply {
                            put("test", "")
                        },
                        JsonNotBlankStringMapper("test"),
                        false,
                        ""
                ),
                StringValidateData(
                        "type = Object, value = null",
                        "test",
                        factory.objectNode().apply {
                            set("test", null)
                        },
                        JsonStringMapper("test"),
                        false,
                        ""
                ),
                StringValidateData(
                        "Object without fields",
                        "test",
                        factory.objectNode(),
                        JsonStringMapper("test"),
                        false,
                        ""
                ),
                StringValidateData(
                        "type = Array",
                        "test",
                        factory.arrayNode(),
                        JsonStringMapper("test"),
                        false,
                        ""
                ),
                StringValidateData(
                        "type = nullNode, value = 'value'",
                        "test",
                        factory.nullNode(),
                        JsonStringMapper("test"),
                        false,
                        ""
                )
        )

        return testData.map {
            dynamicTest("Test String mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expect, it.mapper.map(HashMap(), it.node, target))
                if (it.expect) {
                    assertEquals(it.expectResult, target.path(it.path).asText())
                }

            }
        }.toList()
    }

    @TestFactory
    fun `test json Array mapper`(): Collection<DynamicTest> {
        data class ArrayValidateData(val caseName: String,
                                     val filed: String,
                                     val source: JsonNode,
                                     val mapper: JsonArrayMapper,
                                     val expected: Boolean,
                                     val expectedResult: JsonNode)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                ArrayValidateData(
                        "type = Array, size = 0, required = true, minSize = 0 ",
                        "array",
                        factory.objectNode().apply {
                            putArray("array")
                        },
                        JsonArrayMapper("array"),
                        true,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "type = Array, size = 0, required = false, minSize = 0 ",
                        "array",
                        factory.objectNode().apply {
                            putArray("array")
                        },
                        JsonArrayMapper("array"),
                        true,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "type = Array, size = 0, required = true, minSize = 1 ",
                        "array",
                        factory.objectNode().apply {
                            putArray("array")
                        },
                        JsonArrayMapper("array", nodePredicate = { it -> it.size() > 0 }),
                        false,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "type = Array, size = 1, required = true, minSize = 1 ",
                        "array",
                        factory.objectNode().apply {
                            putArray("array").add("it")
                        },
                        JsonArrayMapper("array", nodePredicate = { it -> it.size() > 0 }),
                        true,
                        factory.arrayNode().add("it")
                ),
                ArrayValidateData(
                        "type = Object, size = 0, required = true, minSize = 0 ",
                        "array",
                        factory.objectNode().apply {
                            putObject("array")
                        },
                        JsonArrayMapper("array"),
                        false,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "type = NullNode, size = 0, required = true, minSize = 0 ",
                        "array",
                        factory.objectNode().apply {
                            set("array", factory.nullNode())
                        },
                        JsonArrayMapper("array"),
                        false,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "type = null, size = 0, required = true, minSize = 0 ",
                        "array",
                        factory.objectNode().apply {
                            set("array", null)
                        },
                        JsonArrayMapper("array"),
                        false,
                        factory.arrayNode()
                ),
                ArrayValidateData(
                        "empty Object ",
                        "array",
                        factory.objectNode(),
                        JsonArrayMapper("array"),
                        false,
                        factory.arrayNode()
                )
        )

        return testData.map {
            dynamicTest("Test Array mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expected, it.mapper.map(HashMap(), it.source, target))
                if (it.expected) {
                    assertEquals(it.expectedResult, target.path(it.filed))
                }

            }
        }.toList()
    }

    @TestFactory
    fun `test json Enum mapper`(): Collection<DynamicTest> {

        data class EnumValidateData(val caseName: String,
                                    val filed: String,
                                    val source: JsonNode,
                                    val mapper: JsonEnumMapper,
                                    val expected: Boolean,
                                    val expectedResult: String)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                EnumValidateData(
                        "type = Object, value = ONE",
                        "enum",
                        factory.objectNode().apply {
                            put("enum", "ONE")
                        },
                        JsonEnumMapper("enum", Test.ONE.toLowerCase()),
                        true,
                        "ONE"
                ),
                EnumValidateData(
                        "type = Object, value = TWO",
                        "enum",
                        factory.objectNode().apply {
                            put("enum", "TWO")
                        },
                        JsonEnumMapper("enum", Test.ONE.toLowerCase()),
                        false,
                        "ONE"
                )
        )

        return testData.map {
            dynamicTest("Test Array mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expected, it.mapper.map(HashMap(), it.source, target))
                if (it.expected) {
                    assertEquals(it.expectedResult, target.path(it.filed).asText())
                }

            }
        }.toList()
    }

    @TestFactory
    fun `test json Object mapper`(): Collection<DynamicTest> {

        data class ObjectValidateData(val caseName: String,
                                      val filed: String,
                                      val source: JsonNode,
                                      val mapper: JsonObjectMapper,
                                      val expected: Boolean,
                                      val expectedResult: ObjectNode?)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                ObjectValidateData(
                        "{'obj': {'enum':'ONE', 'string':'string'}}",
                        "obj",
                        factory.objectNode().apply {
                            putObject("obj")?.apply {
                                put("enum", "ONE")
                                put("string", "string")
                            }
                        },
                        JsonObjectMapper("obj"),
                        true,
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            put("string", "string")
                        }
                ),
                ObjectValidateData(
                        "{}",
                        "obj",
                        factory.objectNode(),
                        JsonObjectMapper("obj"),
                        false,
                        null
                )
        )

        return testData.map {
            dynamicTest("Test Object mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expected, it.mapper.map(HashMap(), it.source, target))
                if (it.expected) {
                    assertEquals(it.expectedResult, target.path(it.filed))
                }

            }
        }.toList()
    }


    @Suppress("UNCHECKED_CAST")
    @TestFactory
    fun `test OrGroupJsonMapper`(): Collection<DynamicTest> {

        data class OrGroupTestData(val caseName: String,
                                   val source: JsonNode,
                                   val mapper: JsonOrGroupMapper,
                                   val expected: Boolean,
                                   val expectedResult: ObjectNode?)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                OrGroupTestData(
                        "{'enum':'ONE', 'string':'string'}",
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            put("string", "string")
                        },
                        JsonOrGroupMapper("obj",
                                listOf(
                                        JsonEnumMapper("enum", Test.ONE.toLowerCase()) as JsonMapper<JsonNode, JsonNode>,
                                        JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>
                                )

                        ),
                        true,
                        factory.objectNode().apply {
                            put("enum", "ONE")
                        }
                ),
                OrGroupTestData(
                        "{}",
                        factory.objectNode(),
                        JsonOrGroupMapper("obj",
                                listOf(
                                        JsonEnumMapper("enum", Test.ONE.toLowerCase()) as JsonMapper<JsonNode, JsonNode>,
                                        JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>
                                )

                        ),
                        false,
                        null
                )
        )

        return testData.map {
            dynamicTest("Test JsonOrGroup mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expected, it.mapper.map(HashMap(), it.source, target))
                if (it.expected) {
                    assertEquals(it.expectedResult, target)
                }

            }
        }.toList()
    }


    @Suppress("UNCHECKED_CAST")
    @TestFactory
    fun `test AllGroupJsonMapper`(): Collection<DynamicTest> {

        data class AllGroupTestData(val caseName: String,
                                    val source: JsonNode,
                                    val mapper: JsonAllGroupMapper,
                                    val expected: Boolean,
                                    val expectedResult: ObjectNode?)

        val factory = JsonNodeFactory.instance
        val testData = listOf(
                AllGroupTestData(
                        "{'enum':'ONE', 'string':'string', 'obj':{'string':'value', 'int': 1}}",
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            put("string", "string")
                            putObject("obj").apply {
                                put("string", "value")
                                put("int", 1)
                            }
                        },
                        JsonAllGroupMapper("obj",
                                listOf(
                                        JsonEnumMapper("enum", Test.ONE.toLowerCase()) as JsonMapper<JsonNode, JsonNode>,
                                        JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>,
                                        JsonObjectMapper("obj",
                                                JsonAllGroupMapper("innerMapper",
                                                        listOf(
                                                                JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>,
                                                                JsonIntMapper("int") as JsonMapper<JsonNode, JsonNode>
                                                        ))
                                        ) as JsonMapper<JsonNode, JsonNode>
                                )

                        ),
                        true,
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            put("string", "string")
                            putObject("obj").apply {
                                put("string", "value")
                                put("int", 1)
                            }
                        }
                ),
                AllGroupTestData(
                        "{}",
                        factory.objectNode(),
                        JsonAllGroupMapper("obj",
                                listOf(
                                        JsonEnumMapper("enum", Test.ONE.toLowerCase()) as JsonMapper<JsonNode, JsonNode>,
                                        JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>
                                )

                        ),
                        false,
                        factory.objectNode()
                ),
                AllGroupTestData(
                        "Object without value but copyOnError TRUE",
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            putObject("obj").apply {
                                put("string", "value")
                                put("int", 1)
                            }
                        },
                        JsonAllGroupMapper("obj",
                                listOf(
                                        JsonEnumMapper("enum", Test.ONE.toLowerCase()) as JsonMapper<JsonNode, JsonNode>,
                                        JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>,
                                        JsonObjectMapper("obj",
                                                JsonAllGroupMapper("innerMapper",
                                                        listOf(
                                                                JsonStringMapper("string") as JsonMapper<JsonNode, JsonNode>,
                                                                JsonIntMapper("int") as JsonMapper<JsonNode, JsonNode>
                                                        ))
                                        ) as JsonMapper<JsonNode, JsonNode>
                                )
                                , true

                        ),
                        false,
                        factory.objectNode().apply {
                            put("enum", "ONE")
                            putObject("obj").apply {
                                put("string", "value")
                                put("int", 1)
                            }
                        }
                )
        )

        return testData.map {
            dynamicTest("Test JsonAllGroup mapper: ${it.caseName}") {
                val target = factory.objectNode()
                assertEquals(it.expected, it.mapper.map(HashMap(), it.source, target))
                assertEquals(it.expectedResult, target)
            }
        }
    }


    @TestFactory
    fun `success JsonSchemaParser test`(): Collection<DynamicTest> {
        val factory = JsonNodeFactory.instance

        data class TestData(
                val caseName: String,
                val schema: ObjectNode,
                val validatorsCount: Int
        )

        val testDataSet = listOf(
                TestData(
                        "Parse without properties and required",
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("type", "object")
                        },
                        1
                ),
                TestData(
                        "Parse whit properties without required",
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("type", "object")
                            putObject("properties").apply {
                                putObject("name").apply {
                                    put("type", "string")
                                }
                            }
                        },
                        2
                ),
                TestData(
                        "Parse whit properties and required",
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("type", "object")
                            putObject("properties").apply {
                                putObject("name").apply {
                                    put("type", "string")
                                }
                            }
                            putArray("required").apply {
                                add("name")
                            }
                        },
                        3
                )
        )

        return testDataSet.map {
            dynamicTest("Test JsonSchemaParser. Case:${it.caseName}") {
                val validators = JsonSchemaParser.parse(it.schema)
                assertEquals(it.validatorsCount, validators.size)

            }
        }
    }

    @TestFactory
    fun `fail JsonSchemaParser test`(): Collection<DynamicTest> {
        val factory = JsonNodeFactory.instance

        data class TestData(
                val caseName: String,
                val schema: ObjectNode,
                val validatorsCount: Int
        )

        val testDataSet = listOf(
                TestData(
                        "Schema without title",
                        factory.objectNode().apply {
                            put("type", "object")
                        },
                        1
                ),
                TestData(
                        "Schema without type",
                        factory.objectNode().apply {
                            put("title", "Simple")
                        },
                        2
                ),
                TestData(
                        "Schema with required fields but without field descriptions",
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("type", "object")
                            putArray("required").apply {
                                add("name")
                            }
                        },
                        3
                )
        )

        return testDataSet.map {
            dynamicTest("Test JsonSchemaParser. Case:${it.caseName}") {
                assertThrows(JsonValidateException::class.java, {
                    JsonSchemaParser.parse(it.schema)
                })
            }
        }
    }

    @TestFactory
    fun `test Mapping through schema`(): Collection<DynamicTest> {
        val factory = JsonNodeFactory.instance

        data class TestData(
                val caseName: String,
                val schema: ObjectNode,
                val source: ObjectNode,
                val expected: ObjectNode
        )

        val testDataSet = listOf(
                TestData(
                        "Simple mapping with required",
                        JsonSchemaBuilder().apply {
                            title = "Simple"
                            type = JsonType.OBJECT
                            addRequired("name", "age")
                            newProperty("name", JsonType.STRING)
                            newProperty("age", JsonType.INTEGER)
                        }.build(),
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("name", "Vlad")
                            put("age", 34)
                        },
                        factory.objectNode().apply {
                            put("title", "Simple")
                            put("name", "Vlad")
                            put("age", 34)
                        }
                )
        )

        return testDataSet.map {
            val testData = it
            dynamicTest("Test Mapping through SchemaValidator. Case:${it.caseName}") {
                val mappings = JsonSchemaParser.parse(it.schema)
                val target = factory.objectNode()
                val errors = HashMap<String, String>()
                mappings.forEach{ it.map(errors, testData.source, target)}
                assertTrue(errors.isEmpty(), errors.toString())
                assertEquals(testData.expected, target)
            }
        }
    }
}