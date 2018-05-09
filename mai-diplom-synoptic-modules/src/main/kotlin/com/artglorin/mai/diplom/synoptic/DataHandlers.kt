package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.DataHandlerModule
import com.artglorin.mai.diplom.core.JsonNodeObservableImpl
import com.artglorin.mai.diplom.core.Processor
import com.artglorin.mai.diplom.core.Settingable
import com.artglorin.mai.diplom.json.*
import com.fasterxml.jackson.databind.JsonNode
import java.util.*
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

open class SimpleAnswerDataHandler: DataHandlerModule, Settingable, Processor {

    private var answers: List<String>? = null
    private val random = Random()
    private val outId = lazy { "${getModuleId()}.out"}
    private val inId = lazy { "${getModuleId()}.out"}

    private val listeners = lazy {
        JsonNodeObservableImpl()
    }

    override fun process(data: JsonNode) {
        if (listeners.isInitialized() && answers != null) {
            answers?.let { it[random.nextInt(it.size)] }
                    ?.let { JacksonNodeFactory.createModuleResult(outId.value, it) }
                    ?.apply (listeners.value::notify)
        }
    }

    override fun applySettings(settings: JsonNode) {
        answers = AppJsonMappers.ignoreUnknown.treeToValue(settings, Settings::class.java)?.answers
    }

    override fun getOutputSchema(): JsonNode {
        return JsonSchemaBuilder().apply {
            title = outId.value
            type = ObjectType
            property("answer", StringType)
            addRequired("answer")
        }.build()
    }

    override fun addObserver(observer: Consumer<JsonNode>) {
        listeners.value.addObserver(observer)
    }

    override fun getInputSchema(): JsonNode {
        return JsonSchemaBuilder().apply {
            title = inId.value
            type = ObjectType
        }.build()
    }

    data class Settings(var answers: List<String>? = null)
}

class Optimist: SimpleAnswerDataHandler()
class Pessimist: SimpleAnswerDataHandler()
class Synoptic: SimpleAnswerDataHandler()
class Mathematician: SimpleAnswerDataHandler()