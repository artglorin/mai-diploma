package com.artglorin.mai.diplom.synoptic.modules.taskmanagers

import com.artglorin.mai.diplom.core.api.*
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */

class SimpleTaskManger : TaskManagerModule {
    var sources: List<DataSourceModule>? = null

    override fun setData(data: TaskManagerData) {
        sources = data.sources
        val handlersMap: Map<String, Module> = ArrayList<Module>(data.handlers)
                .apply {
                    add(data.solutionModule)
                    addAll(data.sources)
                    addAll(data.observables)
                }
                .stream()
                .collect(Collectors.toMap({ it.getModuleId() }, { it -> it }))
        val inputModules = HashMap<String, MutableList<InputModule>>()
        val outputModules = HashMap<String, MutableList<OutputModule>>()
        val batchProcessor = HashMap<String, MutableList<BatchProcessor>>()
        data.dataFlow.forEach {
            val flow = it
            handlersMap[flow.moduleId]?.apply {
                if (this is InputModule) {
                    flow.inputId.forEach {
                        inputModules.getOrPut(it, ::ArrayList).add(this)
                    }
                }
                if (this is BatchProcessor) {
                    flow.inputId.forEach {
                        batchProcessor.getOrPut(it, ::ArrayList).add(this)
                    }
                }
                if (this is OutputModule) {
                    flow.outputId.forEach {
                        outputModules.getOrPut(it, ::ArrayList).add(this)
                    }
                }

            }
        }
        data.pipes.forEach {
            val pipe = it

            inputModules[pipe.id]
                    ?.forEach {
                        val module = it
                        pipe.addListener(Consumer { module.push(it) })
                    }
            outputModules[pipe.id]
                    ?.onEach {
                        it.addListener(Consumer { pipe.push(it) })
                    }
                    ?.apply {
                        val partsCount = this.size
                        batchProcessor[pipe.id]
                                ?.apply {
                                    val batchProcessors = this
                                    pipe.addListener(BatchConsumer(partsCount, batchProcessors))
                                }
                    }
        }
    }

    private class BatchConsumer(
            private val partsCount: Int,
            private val listeners: List<BatchProcessor>
            ) : Consumer<ObjectNode> {

        private val seriesHolder: MutableMap<String, SeriesHolder> = HashMap()

        override fun accept(t: ObjectNode) {
            val seriesId = t.get("seriesId").textValue()
            if (seriesHolder
                            .getOrPut(seriesId, { SeriesHolder(partsCount) })
                            .add(t)) {
                seriesHolder
                        .remove(seriesId)
                        .apply {
                            this?:return
                            val parts = this.parts
                            listeners.forEach { it.push(parts) }
                        }

            }
        }
    }

    private class SeriesHolder(val partsCount: Int) {
        val parts = HashSet<ObjectNode>(partsCount)

        fun add(item: ObjectNode): Boolean {
            parts.add(item)
            return parts.size == partsCount
        }
    }

    override fun process() {
        sources?.stream()
                ?.parallel()
                ?.forEach(DataSourceModule::launch)
                ?: throw IllegalStateException("sources modules is not specified")
    }
}