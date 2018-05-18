package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.Customizable
import com.artglorin.mai.diplom.core.DataHandlerModule
import com.artglorin.mai.diplom.core.JsonNodeListenersContainer
import com.artglorin.mai.diplom.json.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

open class SimpleAnswerDataHandler : DataHandlerModule, Customizable {

    private var answers: List<String>? = null
    private val listeners = lazy {
        JsonNodeListenersContainer()
    }

    private val random = Random()
    private val outId = lazy { "${getModuleId()}.out" }
    private val inId = lazy { "${getModuleId()}.in" }

    override fun addListener(listener: Consumer<ObjectNode>) {
        listeners.value.addObserver(listener)
    }

    override fun push(node: ObjectNode) {
        val answers = answers ?: throw IllegalStateException("Answers must be specified")
        if (listeners.isInitialized()) {
            listeners.value.notify((answers.let { it[random.nextInt(it.size)] }
                    .let { JacksonNodeFactory.createModuleResult(outId.value, "", it) }))
        }
    }

    override fun applySettings(settings: JsonNode) {
        answers = AppJsonMappers.ignoreUnknown.treeToValue(settings, Settings::class.java)?.answers
    }

    override fun getOutputSchema(): ObjectNode {
        return JsonSchemaBuilder().apply {
            title = outId.value
            type = ObjectType
            property("answer", StringType)
            addRequired("answer")
        }.build()
    }

    override fun getInputSchema(): ObjectNode {
        return JsonSchemaBuilder().apply {
            title = inId.value
            type = ObjectType
        }.build()
    }

    data class Settings(var answers: List<String>? = null)
}

class Optimist : SimpleAnswerDataHandler()
class Pessimist : SimpleAnswerDataHandler()
class Synoptic : SimpleAnswerDataHandler()
class Mathematician : SimpleAnswerDataHandler()