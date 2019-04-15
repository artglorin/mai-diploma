package com.artglorin.mai.diplom.core.api

import com.artglorin.mai.diplom.core.FlowItem
import com.artglorin.mai.diplom.core.Pipe
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.function.Consumer

/**
 * @author V.Verminskiy (develop@artglorin.com)
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

interface DataSourceModule : OutputModule {
    fun launch()
}

interface DataHandlerModule :  InputModule, OutputModule

interface TaskManagerModule : Module {
    fun setData(data: TaskManagerData)
    fun process()
}
data class TaskManagerData (
        val sources: Collection<DataSourceModule>,
        val handlers: Collection<DataHandlerModule>,
        val solutionModule: SolutionModule,
        val observables: Collection<DataObserver>,
        val dataFlow: Collection<FlowItem>,
        val pipes: Collection<Pipe>
)

interface DataObserver : InputModule

interface SolutionModule : BatchProcessor, OutputModule