package com.artglorin.mai.diplom.json

import com.artglorin.mai.diplom.core.ConverterDescription
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 14/05/2018
 */
internal class JsonValueConverterFactoryTest {
    companion object {
        val LOG = LoggerFactory.getLogger(JsonValueConverterFactoryTest::class.java.name)!!
    }

    @TestFactory
    fun `test convert values`(): Collection<DynamicTest> {
        val testData = ObjectMapper()
                .readValue(ClassPathResource("JsonValueConverterTestData.json").url,
                        object: TypeReference<List<ObjectNode>>(){}) as List<ObjectNode>
        return testData.map {
            DynamicTest.dynamicTest(it.get("caseName").asText(), {
                LOG.info("$it")
                val target = it.get("target")
                val settings = it.get("settings")
                JsonValueConverterFactory.create(ConverterDescription(
                        settings.path("sourcePath").textValue(),
                        settings.path("targetPath").textValue(),
                        settings.path("matchValue"),
                        settings.path("mismatchValue"),
                        settings.path("matcherId").textValue(),
                        settings.path("matcherSettings")
                )).transfer(it.get("source"),target)
                assertEquals(it.get("expected"), target)
            })
        }
    }
}