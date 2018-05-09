package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.json.AppJsonMappers
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 08/05/2018
 */
internal class JsonDataSourceTest {

    @Test
    fun `test read from file`() {
        val jsonDataSource = JsonDataSource()
        JsonDataSource.Settings().apply {
            sourceFile = ClassPathResource("test_data.json").file.absolutePath.toString()
            filters = JsonDataSource.Filters(listOf(
                    JsonDataSource.EqualsFilterData("city.country", "US"),
                    JsonDataSource.EqualsFilterData("city.name", "New York")
            ))
            mapperData = listOf(
                    JsonDataSource.MapperData("main.humidity", "humidity"),
                    JsonDataSource.MapperData("main.pressure", "pressure"),
                    JsonDataSource.MapperData("main.temp", "temp"),
                    JsonDataSource.MapperData("wind.deg", "wind.deg"),
                    JsonDataSource.MapperData("wind.speed", "wind.speed"),
                    JsonDataSource.MapperData("clouds.all", "clouds"),
                    JsonDataSource.MapperData("time", "time")
            )
        }.let {
            val mapper = AppJsonMappers.ignoreUnknown
            mapper.writerWithDefaultPrettyPrinter().writeValueAsString(it).let {
                mapper.readTree(it)
            }
        }.let {
            jsonDataSource.applySettings(it)
        }
        assertEquals(2, jsonDataSource.getData().count())
    }
}