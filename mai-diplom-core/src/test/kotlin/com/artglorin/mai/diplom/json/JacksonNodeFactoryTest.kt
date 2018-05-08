package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.math.BigDecimal

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JacksonNodeFactoryTest {

    @TestFactory
    fun `test JacksonNodeFactory test`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val type: String
        )

        return arrayOf(
                TestData("Given: empty string. Expected: throw JsonException",
                        ""
                ),
                TestData("Given: blabla(1). Expected: throw JsonException",
                        "blabla(1)"
                ),
                TestData("Given: integer. Expected: throw JsonException",
                        "integer"
                ),
                TestData("Given: integer(asd). Expected: throw JsonException",
                        "integer(asd)"
                ),
                TestData("Given: long. Expected: throw JsonException",
                        "long"
                ),
                TestData("Given: long(asd). Expected: throw JsonException",
                        "long(asd)"
                ),
                TestData("Given: double. Expected: throw JsonException",
                        "double"
                ),
                TestData("Given: double(asd). Expected: throw JsonException",
                        "double(asd)"
                ),
                TestData("Given: string. Expected: throw JsonException",
                        "string"
                )
        ).map {
            DynamicTest.dynamicTest("Test throw JsonValidationException. Case: ${it.caseName}") {
                assertThrows(JsonException::class.java, {
                    JacksonNodeFactory.create(it.type)
                })
            }
        }
    }

    @TestFactory
    fun `test JacksonNodeFactory success creation`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val type: String,
                val expected: JsonNode
        )

        val factory = JsonNodeFactory.instance
        return arrayOf(
                TestData("Given: integer(1). Expected: JsonNode was created",
                        "integer(1)",
                        factory.numberNode(1)
                ),
                TestData("Given: double(2.0). Expected: JsonNode was created",
                        "double(2.0)",
                        factory.numberNode(BigDecimal(2.0))
                ),
                TestData("Given: object(). Expected: JsonNode was created",
                        "object()",
                        factory.objectNode()
                ),
                TestData("Given: array(). Expected: JsonNode was created",
                        "array()",
                        factory.arrayNode()
                ),
                TestData("Given: string(hello). Expected: JsonNode was created",
                        "string(hello)",
                        factory.textNode("hello")
                ),
                TestData("Given: long(123). Expected: JsonNode was created",
                        "long(123)",
                        factory.numberNode(123L)
                ),
                TestData("Given: integer(). Expected: JsonNode was created",
                        "integer()",
                        factory.numberNode(0)
                ),
                TestData("Given: double(). Expected: JsonNode was created",
                        "double()",
                        factory.numberNode(BigDecimal.ZERO)
                ),
                TestData("Given: object(asdf234rdf). Expected: JsonNode was created",
                        "object(sadfsdf)",
                        factory.objectNode()
                ),
                TestData("Given: array(asdfsdf). Expected: JsonNode was created",
                        "array(asdfsdfsd)",
                        factory.arrayNode()
                ),
                TestData("Given: string(). Expected: JsonNode was created",
                        "string()",
                        factory.textNode("")
                ),
                TestData("Given: long(). Expected: JsonNode was created",
                        "long()",
                        factory.numberNode(0L)
                )
        ).map {
            DynamicTest.dynamicTest("Test throw JsonValidationException. Case: ${it.caseName}") {
                assertEquals(it.expected, JacksonNodeFactory.create(it.type))
            }
        }
    }

    @TestFactory
    fun `test JacksonNodeFactory by type success creation`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val type: ContainerType,
                val expected: JsonNode
        )

        val factory = JsonNodeFactory.instance
        return arrayOf(
                TestData("Given: ObjectType. Expected: JsonNode was created",
                        ObjectType,
                        factory.objectNode()
                ),
                TestData("Given: ContainerType. Expected: JsonNode was created",
                        ArrayType,
                        factory.arrayNode()
                )
        ).map {
            DynamicTest.dynamicTest("Test throw JsonValidationException. Case: ${it.caseName}") {
                assertEquals(it.expected, JacksonNodeFactory.create(it.type))
            }
        }
    }
}