package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.*
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
                }.stream()
                .collect(Collectors.toMap({ it.getModuleId() }, { it -> it }))
        val inputModules = HashMap<String, MutableList<InputModule>>()
        val outputModules = HashMap<String, MutableList<OutputModule>>()
        data.dataFlow.forEach {
            val flow = it
            handlersMap[flow.moduleId]?.apply {
                if (this is InputModule) {
                    flow.inputId.forEach {
                        inputModules.getOrPut(it, ::ArrayList).add(this)
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
            inputModules.filterKeys { pipe.id == it  }.map {
                it.value.forEach{
                    val module = it
                    pipe.addListener(Consumer { module.push(it) })

                }
            }
            outputModules.filterKeys { pipe.id == it  }.map {
                it.value.forEach{
                    it.addListener(Consumer { pipe.push(it) })

                }
            }
        }
    }

    override fun process() {
        sources?.stream()
                ?.parallel()
                ?.forEach(DataSourceModule::launch)
                ?: throw IllegalStateException("sources modules is not specified")
    }
}