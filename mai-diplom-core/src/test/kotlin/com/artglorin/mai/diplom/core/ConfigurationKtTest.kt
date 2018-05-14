package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import java.util.function.BiConsumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */
internal class ConfigurationKtTest {

    open class TestModule: Module, Customizable, SolutionModule {
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

}