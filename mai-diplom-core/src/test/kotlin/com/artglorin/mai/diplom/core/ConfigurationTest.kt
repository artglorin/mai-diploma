package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.MissingNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */
internal class ConfigurationTest {

    open class TestModule : Module, Customizable, SolutionModule {
        override fun addListener(listener: Consumer<JsonNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getOutputSchema(): JsonNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun push(node: JsonNode) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
        override fun getInputSchema(): JsonNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getModuleId() = "module"
        override fun applySettings(settings: JsonNode) {}

    }

    @Test
    fun `test that applySetting was called`() {
        val configuration = Configuration(modules = mapOf(Pair("module", ModuleConfig(ObjectMapper().createObjectNode()))))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(1)).applySettings(any())
    }

    @Test
    fun `test that applySettings was called with settings are null`() {
        val configuration = Configuration(modules = mapOf(Pair("module", ModuleConfig(ObjectMapper().createObjectNode()))))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(1)).applySettings(any())
    }

    @Test
    fun `test that applySettings was not called if module name is dismatch`() {
        val configuration = Configuration(modules = mapOf(Pair("module1", ModuleConfig(ObjectMapper().createObjectNode()))))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(0)).applySettings(any())
    }

    @Test
    fun `test deserialize configuration`() {
        val configuration = ConfigurationLoader({ ClassPathResource("AppConfiguration.json").uri }).loadProperties()
        assertEquals("path", configuration.modulesPath)
        assertTrue(configuration.modules.isNotEmpty())
        assertNotEquals(MissingNode.getInstance(), configuration.modules["one"]?.settings)
        assertEquals(3, configuration.dataFlow.size)
        assertEquals("one", configuration.dataFlow[0].moduleId)
        assertEquals(listOf<String>(), configuration.dataFlow[0].inputId)
        assertEquals(listOf("a-out"), configuration.dataFlow[0].outputId)
        assertEquals("two", configuration.dataFlow[1].moduleId)
        assertEquals(listOf("a-out"), configuration.dataFlow[1].inputId)
        assertEquals(listOf("b-in"), configuration.dataFlow[1].outputId)
        assertEquals("three", configuration.dataFlow[2].moduleId)
        assertEquals(listOf("c-out"), configuration.dataFlow[2].inputId)
        assertEquals(listOf<String>(), configuration.dataFlow[2].outputId)
        assertEquals(1, configuration.pipes.size)
        assertNotNull(1, configuration.pipes[0].id)
        assertEquals(JsonNodeFactory.instance.objectNode(), configuration.pipes[0].filter)
        assertNotNull( configuration.pipes[0].template)
//        assertEquals("one", configuration.pipes[0].copiers[0].from)
//        assertEquals("to", configuration.pipes[0].copiers[0].to)
        assertEquals(1, configuration.pipes[0].converters.size)
        assertEquals(1, configuration.pipes[0].converters.size)
        assertEquals("one", configuration.pipes[0].converters[0].sourcePath)
        assertEquals("two", configuration.pipes[0].converters[0].targetPath)
        assertEquals(JsonNodeFactory.instance.textNode("2"), configuration.pipes[0].converters[0].matchValue)
        assertEquals(JsonNodeFactory.instance.textNode("3"), configuration.pipes[0].converters[0].mismatchValue)
        assertEquals("oo", configuration.pipes[0].converters[0].matcherId)
        assertEquals(JsonNodeFactory.instance.arrayNode().apply {
            addObject()
        }, configuration.pipes[0].converters[0].matcherSettings)
    }

}