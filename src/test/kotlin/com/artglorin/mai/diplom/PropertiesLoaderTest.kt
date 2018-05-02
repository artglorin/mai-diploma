package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */
@Tag(Category.UNIT)
internal class PropertiesLoaderTest {

    @Test
    fun `test fail when source does not exist`() {
        assertThrows(FileNotFoundException::class.java, {
            load(TestData(file = Paths.get("test.json")))
        })
    }

    @Test
    fun `test success when source is exist`() {
        assertNotNull(load(TestData()))
    }

    @Test fun `test that APP_CONFIG file is Null`() {
        assertTrue(PropertiesLoader.APP_CONFIG.loadProperties().isNull)
    }

    private fun load(testData: TestData) = PropertiesLoader({ testData.file.toUri() }).loadProperties()

    @Test
    fun `test correct read value`() {
        assertEquals("test", load(TestData().apply {
            node.put("test", "test")
            save()
        }).get("test").asText())
    }

    private data class TestData(val node: ObjectNode = ObjectMapper().createObjectNode(), val file: Path = Files.createTempFile(null, ".json")) {

        fun save() {
            ObjectMapper().writeValue(file.toFile(), node)
        }
    }
}