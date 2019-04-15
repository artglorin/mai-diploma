package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 05/05/2018
 */
internal class JsonSchemaTest {

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
                            type = ObjectType
                            addRequired("name", "age")
                            property("name", StringType)
                            property("age", IntegerType)
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