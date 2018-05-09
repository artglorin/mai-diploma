package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import java.util.*
import java.util.function.Consumer

class JsonNodeObservableImpl : JsonNodeObservable {
    private val items = object :java.util.Observable() {
        fun notify(node: JsonNode) {
            setChanged()
            notifyObservers(node)
        }

    }

    override fun addObserver(observer: Consumer<JsonNode>) {
        items.addObserver(ConsumeOBserver(observer))
    }

    fun notify(node: JsonNode) {
        items.notify(node)
    }

    private class ConsumeOBserver(private val consume: Consumer<JsonNode>) : Observer {
        override fun update(o: java.util.Observable?, arg: Any?) {
            arg?.takeIf { it is JsonNode }
                    ?.let { it as JsonNode }
                    ?.apply { consume.accept(this) }
        }

    }
}