package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.json.AppJsonMappers
import com.fasterxml.jackson.databind.JsonNode
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.springframework.core.io.ClassPathResource
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JsonDataSourceTest {

    @Test
    fun `test read from file`() {
        val jsonNode = AppJsonMappers.ignoreUnknown.readTree(ClassPathResource("app_config.json").url)
        @Suppress("UNCHECKED_CAST")
        val listener = mock(Consumer::class.java) as Consumer<JsonNode>
        val itemsCount = jsonNode
                .path("modules")
                .path("JsonDataSource")
                .path("settings")
                .let { JsonDataSource().apply {
                    addObserver(listener)
                    applySettings(it) } }.getData().count()
        verify(listener, times(1)).accept(any())
        assertEquals(1, itemsCount)
    }
}