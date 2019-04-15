package com.artglorin.mai.diplom.synoptic.modules.datasources

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.mock
import java.util.function.Consumer

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 17/05/2018
 */
internal class JsonDataSourceTest {

    @Test
    fun `test read data from file`() {
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<ObjectNode>
        val dataSource = JsonDataSource()
        dataSource.applySettings(JsonNodeFactory.instance.objectNode().apply {
            put("sourceFile", "test_data.json")
        })
        dataSource.addListener(listener)
        dataSource.launch()
        verify(listener, times(22635)).accept(ArgumentMatchers.any())
    }
}