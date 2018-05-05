package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */
internal class ConfigurationKtTest {

    open class TestModule: Module, Settingable , SolutionModule{
        override fun getOutputSchema(): JsonNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addObserver(observer: Consumer<JsonNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getInputSchema(): JsonNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getModuleId() = "module"
        override fun applySettings(settings: JsonNode) {}
        override fun getData(): Stream<JsonNode> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    @Test
    fun `test that applySetting was called`() {
        val configuration = Configuration(modules = Modules(solutionModule = ModuleConfig(id = "module", settings = ObjectMapper().createObjectNode())))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(1)).applySettings(any())
    }

    @Test
    fun `test that applySettings was called with settings are null`() {
        val configuration = Configuration(modules = Modules(solutionModule = ModuleConfig(id = "module")))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(1)).applySettings(any())
    }

    @Test
    fun `test that applySettings was not called if module name is dismatch`() {
        val configuration = Configuration(modules = Modules(solutionModule = ModuleConfig(id = "module1")))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(0)).applySettings(any())
    }

    @Test
    fun `test that applySettings and getModuleId were not called if module has another class`() {
        val configuration = Configuration(modules = Modules(taskManager = ModuleConfig(id = "module1")))
        val testModule = TestModule()
        val spy = spy(testModule)
        configuration.configure(spy)
        verify(spy, times(1)).getModuleId()
        verify(spy, times(0)).applySettings(any())
    }
}