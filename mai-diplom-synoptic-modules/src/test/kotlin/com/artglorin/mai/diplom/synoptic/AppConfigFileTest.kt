package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.json.AppJsonMappers
import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 09/05/2018
 */
internal class AppConfigFileTest {

    @Test
    fun `test app_config file`() {
        val jsonNode = AppJsonMappers.ignoreUnknown.readTree(ClassPathResource("app_config.json").url)
        val modulesNode = jsonNode.path("modules")
        val dataSourceNode = modulesNode.path("JsonDataSource").path("settings")
        val dataSourceSettings = AppJsonMappers.ignoreUnknown.treeToValue(dataSourceNode, JsonDataSource.Settings::class.java)
        assertNotNull(dataSourceSettings.sourceFile)
        assertTrue(dataSourceSettings.mappers?.isNotEmpty() ?: false)
        assertTrue(dataSourceSettings.filters?.equals?.isNotEmpty() ?: false)
        testSimpleDataHandlerSettings(modulesNode, "Optimist")
        testSimpleDataHandlerSettings(modulesNode, "Pessimist")
        testSimpleDataHandlerSettings(modulesNode, "Synoptic")
    }

    private fun testSimpleDataHandlerSettings(modules: JsonNode, name: String) {
        val settings = AppJsonMappers.ignoreUnknown.treeToValue(modules.path(name).path("settings"), SimpleAnswerDataHandler.Settings::class.java)
        assertTrue(settings.answers?.isNotEmpty()?: false)
    }
}