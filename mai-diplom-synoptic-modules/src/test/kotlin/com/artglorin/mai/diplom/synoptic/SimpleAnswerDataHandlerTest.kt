package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.json.JacksonNodeFactory
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 09/05/2018
 */
internal class SimpleAnswerDataHandlerTest {

    @Test
    fun `test listeners not invokes if answers is not specified`() {
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<JsonNode>
        val node = JsonNodeFactory.instance.objectNode()
        val module = SimpleAnswerDataHandler().apply { addObserver(listener) }
        for (i in 1..5) {
            module.process(node)
        }
        verify(listener, times(0)).accept(any())
    }

    @Test
    fun `test listeners not invokes if answers is specified`() {
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<JsonNode>
        val node = JsonNodeFactory.instance.objectNode()
        val module = SimpleAnswerDataHandler().apply {
            addObserver(listener)
            applySettings(JacksonNodeFactory.toJson(SimpleAnswerDataHandler.Settings(answers = listOf("2"))))
        }
        for (i in 1..5) {
            module.process(node)
        }
        verify(listener, times(5)).accept(any())
    }
}