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
import java.util.function.BiConsumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */
internal class ConfigurationTest {

    open class TestModule : Module, Customizable, SolutionModule {
        override fun process(data: List<JsonNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addObserver(observer: BiConsumer<Module, JsonNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getOutputSchema(): JsonNode {
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
        assertEquals("", configuration.dataFlow[0].inputId)
        assertEquals("a-out", configuration.dataFlow[0].outputId)
        assertEquals(false, configuration.dataFlow[0].wholeSeries)
        assertEquals("two", configuration.dataFlow[1].moduleId)
        assertEquals("a-out", configuration.dataFlow[1].inputId)
        assertEquals("b-in", configuration.dataFlow[1].outputId)
        assertEquals(false, configuration.dataFlow[1].wholeSeries)
        assertEquals("three", configuration.dataFlow[2].moduleId)
        assertEquals("c-out", configuration.dataFlow[2].inputId)
        assertEquals("", configuration.dataFlow[2].outputId)
        assertEquals(true, configuration.dataFlow[2].wholeSeries)
        assertEquals(1, configuration.pipes.size)
        assertEquals(1, configuration.pipes[0].inputId.size)
        assertEquals("a-in", configuration.pipes[0].inputId[0])
        assertEquals(1, configuration.pipes[0].outputId.size)
        assertEquals("a-out", configuration.pipes[0].outputId[0])
        assertEquals(1, configuration.pipes[0].filters.size)
        assertEquals(JsonNodeFactory.instance.objectNode(), configuration.pipes[0].filters[0])
        assertEquals(1, configuration.pipes[0].copiers.size)
        assertEquals("one", configuration.pipes[0].copiers[0].from)
        assertEquals("to", configuration.pipes[0].copiers[0].to)
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