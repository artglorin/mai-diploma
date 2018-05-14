package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import java.util.function.BiConsumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

interface Module {
    fun getModuleId(): String = this::javaClass.name
}

interface Processor {
    fun process(data: JsonNode)
}

interface BatchProcessor {
    fun process(data: List<JsonNode>)
}

interface Customizable {
    fun applySettings(settings: JsonNode)
}

interface OutputModule : Module {
    fun getOutputSchema(): JsonNode
}

interface InputModule : Module {
    fun getInputSchema(): JsonNode
}

interface DataSourceModule : ObservableModule {
    fun getData(): Stream<JsonNode>
}

interface DataHandlerModule : ObservableModule, InputModule, OutputModule

interface TaskManagerModule : Module {
    fun setSources(source: List<DataSourceModule>)
    fun setHandlers(handler: List<DataHandlerModule>)
    fun setSolution(solutionModule: SolutionModule)
    fun process()
}

interface ObservableModule : Module {
    fun addObserver(observer: BiConsumer<Module, JsonNode>)
}

interface DataObserver : BiConsumer<Module, JsonNode>, Module {
    fun getObservablesIds(): Collection<String>
}

interface SolutionModule : ObservableModule, OutputModule, InputModule {
    fun process(data: List<JsonNode>)
}