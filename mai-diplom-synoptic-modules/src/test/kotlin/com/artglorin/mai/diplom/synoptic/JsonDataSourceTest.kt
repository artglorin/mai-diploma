package com.artglorin.mai.diplom.synoptic

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */
internal class JsonDataSourceTest {

    @Test
    fun `test read data from file`() {
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<JsonNode>
        val dataSource = JsonDataSource()
        dataSource.applySettings(JsonNodeFactory.instance.objectNode().apply {
            put("sourceFile", "test_data.json")
        })
        dataSource.addListener(listener)
        dataSource.launch()
        verify(listener, times(22635)).accept(ArgumentMatchers.any())
    }
}