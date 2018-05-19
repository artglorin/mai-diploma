package com.artglorin.mai.diplom.synoptic.modules.taskmangers

import com.artglorin.mai.diplom.core.*
import com.artglorin.mai.diplom.json.JacksonNodeFactory
import com.artglorin.mai.diplom.synoptic.modules.taskmanagers.SimpleTaskManger
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.spy
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */
internal class SimpleTaskMangerTest {
    open class TestHandler(private val name: String) : DataHandlerModule, DataObserver, DataSourceModule {
        private val listeners = lazy {

            JsonNodeListenersContainer()
        }

        override fun addListener(listener: Consumer<ObjectNode>) {
            listeners.value.addObserver(listener)
        }

        override fun launch() {

            listeners.value.notify(JacksonNodeFactory.createModuleResult(getModuleId(), "1", "")
            )
        }

        override fun getModuleId(): String {
            return name
        }

        override fun getInputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun push(node: ObjectNode) {

            listeners.value.notify(JacksonNodeFactory.createModuleResult(getModuleId(), node.get("seriesId").textValue(), node))
        }

        override fun getOutputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    open class TestBatchProcessor(private val name: String) : SolutionModule {
        override fun getModuleId(): String {
            return name
        }

        private val listeners = lazy {

            JsonNodeListenersContainer()
        }

        override fun addListener(listener: Consumer<ObjectNode>) {
            listeners.value.addObserver(listener)
        }

        override fun getOutputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun push(data: Collection<ObjectNode>) {
            data.forEach {

                listeners.value.notify(JacksonNodeFactory.createModuleResult(getModuleId(), it.get("seriesId").textValue(), it))
            }
        }

        override fun getInputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }


    @Test
    fun `test task manager`() {

        val taskManger = SimpleTaskManger()
        val source = spy(TestHandler("source"))
        val modA = spy(TestHandler("A"))
        val modB = spy(TestHandler("B"))
        val modC = spy(TestHandler("C"))
        val solution = spy(TestBatchProcessor("solution"))
        val l1 = spy(TestHandler("listener1"))
        val l2 = spy(TestHandler("listener2"))
        taskManger.setData(
                TaskManagerData(
                        listOf(
                                source),
                        listOf(
                                modA,
                                modB,
                                modC
                        ),
                        solution,
                        listOf(
                                l1,
                                l2
                        ),
                        listOf(
                                FlowItem("source", outputId = listOf("source-pipe")),
                                FlowItem("A", listOf("source-pipe"), listOf("solution-in")),
                                FlowItem("B", listOf("source-pipe"), listOf("solution-in", "b-pipe")),
                                FlowItem("C", listOf("b-pipe"), listOf("c-pipe")),
                                FlowItem("solution", listOf("solution-in"), listOf("solution-out")),
                                FlowItem("listener1", listOf("source-pipe", "solution-out")),
                                FlowItem("listener2", listOf("c-pipe"))
                        ),
                        listOf(
                                Pipe("source-pipe"),
                                Pipe("solution-in"),
                                Pipe("solution-out"),
                                Pipe("b-pipe"),
                                Pipe("c-pipe")
                        )
                )
        )
        taskManger.process()
        verify(modA, times(1)).push(any())
        verify(modB, times(1)).push(any())
        verify(modC, times(1)).push(any())
        verify(solution, times(1)).push(any<Collection<ObjectNode>>())
        verify(l1, times(3)).push(any())
        verify(l2, times(1)).push(any())

    }
}