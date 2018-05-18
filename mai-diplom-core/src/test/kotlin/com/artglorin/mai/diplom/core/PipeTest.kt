package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.json.JsonFilterFactory
import com.artglorin.mai.diplom.json.JsonValueConverterFactory
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */
internal class PipeTest {

    @Test
    fun `test filter in pipe`() {
        val factory = JsonNodeFactory.instance
        val pipe = Pipe(
                "pipe",
                JsonFilterFactory.create(factory.objectNode().apply {
                    put("type", "simple")
                    putObject("matcher").apply {
                        put("name", "equals")
                        put("template", 1)

                    }
                    put("sourcePath", "obj")
                })
        )
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<ObjectNode>
        pipe.addListener(listener)
        pipe.push(factory.objectNode().apply {
            put("obj", 1)
        })
        pipe.push(factory.objectNode().apply {
            put("obj", 1)
        })
        pipe.push(factory.objectNode().apply {
            put("obj", 2)
        })
        verify(listener, times(2)).accept(any())
    }
    @Test
    fun `test converters in pipe`() {
        val factory = JsonNodeFactory.instance
        val pipe = Pipe(
                "pipe",
                template = factory.objectNode(),
                converters = listOf(JsonValueConverterFactory.create(ConverterDescription(
                        "obj",
                        "tt",
                        factory.objectNode().apply { put("new", "match") },
                        mismatchValue = factory.arrayNode(),
                        matcherId = "equals",
                        matcherSettings = factory.objectNode().apply {
                            put("name", "equals")
                            put("template", 2)
                        }

                )))
        )
        @Suppress("UNCHECKED_CAST")
        val listener = object: Consumer<ObjectNode> {
            val items = ArrayList<ObjectNode>()

            override fun accept(t: ObjectNode) {
                items.add(t)
            }
        }
        pipe.addListener(listener)

        pipe.push(factory.objectNode().apply {
            put("obj", 2)
        })
        assertEquals(factory.objectNode().apply {
            putObject("tt").put("new","match")
        }, listener.items.last())

        pipe.push(factory.objectNode().apply {
            put("obj", 1)
        })
        assertEquals(factory.objectNode().apply {
            putArray("tt")
        }, listener.items.last())

    }
}
