package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

interface Module {
    fun getModuleId(): String = this::javaClass.name
}

interface Processor {
    fun process(data: JsonNode) : JsonNode
}

interface Customizable {
    fun applySettings(settings: JsonNode)
}

interface OutputModule : Module {
    fun getOutputSchema(): JsonNode
    fun addListener (listener: Consumer<JsonNode>)
}

interface InputModule : Module {
    fun getInputSchema(): JsonNode
    fun push(node: JsonNode)
}

interface DataSourceModule : Module, OutputModule {
    fun launch()
}

interface DataHandlerModule :  InputModule, OutputModule

interface TaskManagerModule : Module{
    fun setData(data: TaskManagerData)
    fun process()
}
data class TaskManagerData (
        val sources: List<DataSourceModule>,
        val handlers: List<DataHandlerModule>,
        val solutionModule: SolutionModule,
        val observables: List<DataObserver>,
        val dataFlow: List<FlowItem>,
        val pipes: List<Pipe>
)

interface DataObserver :  InputModule

interface SolutionModule : InputModule, OutputModule