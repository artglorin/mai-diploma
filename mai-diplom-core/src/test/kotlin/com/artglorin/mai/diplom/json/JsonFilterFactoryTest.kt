package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.core.io.ClassPathResource

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 15/05/2018
 */
internal class JsonFilterFactoryTest {
    @TestFactory
    fun `test filtering`(): Collection<DynamicTest> {
        val testData = ObjectMapper().readTree(ClassPathResource("JsonFilterTestData.json").url)

        return   (testData.get("data") as ArrayNode)
                .map {
                    DynamicTest.dynamicTest(it.get("caseName").textValue(), {

                        val filter = JsonFilterFactory.create(it.get("settings") as ObjectNode)
                        assertEquals(it.get("expectedSize").asInt(), (testData.get("items") as ArrayNode).filter { filter.pass(it) }.size)
                    })
                }
    }
}