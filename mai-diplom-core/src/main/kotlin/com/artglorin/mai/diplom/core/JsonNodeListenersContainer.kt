package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.JsonNode
import java.util.*
import java.util.function.Consumer

class JsonNodeListenersContainer{
    private val items = object :java.util.Observable() {
        fun notify(node: JsonNode) {
            setChanged()
            notifyObservers(node)
        }

    }


     fun addObserver(observer: Consumer< JsonNode>) {
        items.addObserver(ConsumeObserver(observer))
    }

    fun notify(node: JsonNode) {
        items.notify(node)
    }

    inner class ConsumeObserver(private val consume: Consumer<JsonNode>) : Observer {
        override fun update(o: java.util.Observable?, arg: Any?) {
            arg?.takeIf { it is JsonNode }
                    ?.let { it as JsonNode }
                    ?.apply { consume.accept(this) }
        }

    }
}