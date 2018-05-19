package com.artglorin.mai.diplom.synoptic.modules.datahandlers

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */
internal class SimpleAnswerDataHandlerTest {
    @Test
    fun `test read data from file`() {
        val dataHandler = SimpleAnswerDataHandler()
        @Suppress("UNCHECKED_CAST")
        val listener = Mockito.mock(Consumer::class.java) as Consumer<ObjectNode>
        dataHandler.addListener(listener)
        val node = JsonNodeFactory.instance.objectNode().apply {
            put("seriesId", "0")
            putArray("answers").apply {
                add("hello")
                add("world")
            }
        }
        dataHandler.applySettings(node)
        for (i in 1..3) {
            dataHandler.push(node)
        }
        verify(listener, times(3)).accept(any())
    }
}