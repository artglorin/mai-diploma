package com.artglorin.mai.diplom.core

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.*
import java.util.function.Consumer

class JsonNodeListenersContainer{
    private val items = object :java.util.Observable() {
        fun notify(node: ObjectNode) {
            setChanged()
            notifyObservers(node)
        }

    }


     fun addObserver(observer: Consumer<ObjectNode>) {
        items.addObserver(ConsumeObserver(observer))
    }

    fun notify(node: ObjectNode) {
        items.notify(node)
    }

    inner class ConsumeObserver(private val consume: Consumer<ObjectNode>) : Observer {
        override fun update(o: java.util.Observable?, arg: Any?) {
            arg?.takeIf { it is ObjectNode }
                    ?.let { it as ObjectNode }
                    ?.apply { consume.accept(this) }
        }

    }
}