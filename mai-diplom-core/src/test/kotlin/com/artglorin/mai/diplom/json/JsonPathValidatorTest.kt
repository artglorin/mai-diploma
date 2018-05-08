package com.artglorin.mai.diplom.json

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JsonPathValidatorTest {
    @TestFactory
    fun `test JsonPathValidationException is throws in Node$createPath`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val path: String
        )
        return arrayOf(
                TestData("Given empty path. Expect throw JsonValidationException",
                        ""
                ),
                TestData("Given path with chars in a squire brackets: path[qwer]. Expect throw JsonValidationException" ,
                        "path[wer]"
                ),
                TestData("Given path with cars between numbers in a squire brackets: array[123abc321]. Expect throw JsonValidationException",
                        "array[123abc321]"
                ),
                TestData("Given path with cars in before numbers in a squire brackets: array[abc123]. Expect throw JsonValidationException",
                        "array[abc123]"
                ),
                TestData("Given path with dot in a squire brackets: array[abc.123]. Expect throw JsonValidationException",
                        "array[abc.123]"
                ),
                TestData("Given path with cars in after numbers in a squire brackets: array[abc123]. Expect throw JsonValidationException",
                        "array[123abc]"
                )
        ).map {
            DynamicTest.dynamicTest("Test throw JsonValidationException. Case: ${it.caseName}") {
                assertThrows(JsonException::class.java, {
                    JsonPathValidator.validate(it.path)
                })
            }
        }
    }

    @TestFactory
    fun `test correct paths`(): Collection<DynamicTest> {
        data class TestData(
                val caseName: String,
                val path: String
        )
        return arrayOf(
                TestData("Empty Path",
                        ""
                ),
                TestData("Path with one node and without equals sign",
                        "path[wer]"
                )
        ).map {
            DynamicTest.dynamicTest("Test throw JsonValidationException. Case: ${it.caseName}") {
                assertThrows(JsonException::class.java, {
                    JsonPathValidator.validate(it.path)
                })
            }
        }
    }
}