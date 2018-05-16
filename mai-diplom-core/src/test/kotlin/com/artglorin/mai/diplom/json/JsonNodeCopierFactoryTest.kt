package com.artglorin.mai.diplom.json

import com.artglorin.mai.diplom.core.CopierConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.core.io.ClassPathResource

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 16/05/2018
 */
internal class JsonNodeCopierFactoryTest {
    @TestFactory
    fun `test mapping objects`(): Collection<DynamicTest> {
        val testData = ObjectMapper().readTree(ClassPathResource("JsonNodeCopierFactoryTestData.json").url)
        return (testData.get("data") as ArrayNode)
                .map {
                    DynamicTest.dynamicTest(it.get("caseName").textValue(), {
                        val target = it.get("target")

                        val settings = it.get("settings")
                        JsonNodeCopierFactory.create(CopierConfig(settings.get("from").textValue(), settings.get("to").textValue())).transfer(it.get("source"), target)
                        assertEquals(it.get("expected"), target)
                    })
                }
    }


}