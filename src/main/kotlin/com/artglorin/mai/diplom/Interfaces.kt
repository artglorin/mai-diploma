package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.JsonNode
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

data class Mapper(val sourceId: String,
                  val sourceIdPath: String,
                  val sourcePath: String,
                  val targetPath: String)

interface DataMapper {
    fun setFieldMapping(mappings: List<Mapper>?)
    fun canMap(json: JsonNode): Boolean
    fun map(source: JsonNode): JsonNode
}

interface Module {
    fun getModuleId(): String = this::javaClass.name
}

interface ComparableModule {
    fun getComparator(): Comparator<JsonNode>
}

interface Processor {
    fun process(data: JsonNode)
}

interface BatchProcessor {
    fun batchProcess(data: List<JsonNode>)
}

interface Settingable {
    fun applySettings(settings: JsonNode)
}

interface Mapperable {
    fun getDataMapper(): DataMapper
}

interface OutputModule: Module {
    fun getOutputSchema(): JsonNode
    fun addObserver(observer: Consumer<JsonNode>)
}

interface InputModule: Module {
    fun getInputSchema(): JsonNode
}

enum class WorkStatus {
    IDLE, IN_PROGRESS, FINISHED
}

interface DataSourceModule : Module, OutputModule {
    fun getData(): Stream<JsonNode>
}

interface DataHandlerModule : Module, Mapperable, InputModule, OutputModule

interface TaskManagerModule : Module, Settingable {
    fun addSources(source: List<DataSourceModule>)
    fun addHandlers(handler: List<DataHandlerModule>)
    fun process()
}

interface DataObserver : Consumer<JsonNode>, Module {
    fun getObservablesIds(): Collection<String>
}

interface SolutionModule : Module, Settingable, OutputModule, InputModule {
    fun getData(): Stream<JsonNode>
}