package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
        assertTrue(configuration.filters.isNotEmpty())
        assertEquals(1, configuration.filters.size)
        assertEquals(1, configuration.filters[0].producers.size)
        assertEquals(3, configuration.filters[0].consumers.size)
        assertNotEquals(MissingNode.getInstance(), configuration.filters[0].filter)
        assertTrue(configuration.copiers.isNotEmpty())
        assertEquals(1, configuration.copiers.size)
        assertEquals(1, configuration.copiers[0].producers.size)
        assertEquals(3, configuration.copiers[0].consumers.size)
        assertNotEquals(MissingNode.getInstance(), configuration.copiers[0].template)
        assertEquals(1, configuration.copiers[0].paths.size)
        assertNotEquals(MissingNode.getInstance(), configuration.copiers[0].paths[0])
    }

}