package com.artglorin.mai.diplom.synoptic.modules.solutions

import com.artglorin.mai.diplom.core.JsonNodeListenersContainer
import com.artglorin.mai.diplom.core.api.Customizable
import com.artglorin.mai.diplom.core.api.SolutionModule
import com.artglorin.mai.diplom.json.AppJsonMappers
import com.artglorin.mai.diplom.json.IntegerType
import com.artglorin.mai.diplom.json.JsonSchemaBuilder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 09/05/2018
 */

class WeightsSolutionModule : SolutionModule, Customizable {
    private val weights = TreeSet<ModuleWeight>()
    private val listeners = lazy { JsonNodeListenersContainer() }
    private val inId = lazy { "${getModuleId()}.in" }
    private val outId = lazy { "${getModuleId()}.out" }

    override fun addListener(listener: Consumer<ObjectNode>) {
        listeners.value.addObserver(listener)
    }

    override fun applySettings(settings: JsonNode) {
        AppJsonMappers.ignoreUnknown.treeToValue(settings, Settings::class.java)
                ?.also {
                    it.weights?.apply {
                        weights.addAll(this)
                    }
                }
    }

    override fun push(data: Collection<ObjectNode>) {
        val groupBy: Map<String, ObjectNode> = data
                .stream()
                .collect(Collectors.toMap({ it -> it.get("moduleId").asText() }, { it -> it }))

        for (weight in weights) {
            val node = groupBy[weight.moduleId]
            if (node != null) {
                listeners.value.notify(node)
            }
        }
    }

    override fun getInputSchema(): ObjectNode {
        return buildScheme(inId.value)
    }

    override fun getOutputSchema(): ObjectNode {
        return buildScheme(outId.value)
    }

    private fun buildScheme(id: String): ObjectNode {
        return JsonSchemaBuilder().apply {
            title = id
            property("moduleId", IntegerType)
            addRequired("moduleId")
        }.build()
    }

    data class Settings(
            var weights: Collection<ModuleWeight>? = null
    )

    data class ModuleWeight (
            var weight: Double? = null,
            var moduleId: String? = null
    ): Comparable<ModuleWeight> {
        override fun compareTo(other: ModuleWeight): Int  {
                return if (weight == other.weight) {
                        0
                    } else if (weight != null && other.weight != null) {
                        weight!!.compareTo(other.weight!!).unaryMinus()
                    } else if (weight == null) {
                        1
                    } else {
                        -1
                    }
                }
    }
}