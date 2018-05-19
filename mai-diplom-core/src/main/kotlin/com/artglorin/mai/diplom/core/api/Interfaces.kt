package com.artglorin.mai.diplom.core.api

import com.artglorin.mai.diplom.core.FlowItem
import com.artglorin.mai.diplom.core.Pipe
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

interface Module {
    fun getModuleId(): String {
        return this::class.java.simpleName
    }
}

interface BatchProcessor: Module {
    fun getInputSchema(): ObjectNode
    fun push(data: Collection<ObjectNode>)
}

interface Customizable {
    fun applySettings(settings: JsonNode)
}

interface OutputModule : Module {
    fun getOutputSchema(): ObjectNode
    fun addListener (listener: Consumer<ObjectNode>)
}

interface InputModule : Module {
    fun getInputSchema(): ObjectNode
    fun push(node: ObjectNode)
}

interface DataSourceModule : Module, OutputModule {
    fun launch()
}

interface DataHandlerModule :  InputModule, OutputModule

interface TaskManagerModule : Module {
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

interface DataObserver : InputModule

interface SolutionModule : BatchProcessor, OutputModule