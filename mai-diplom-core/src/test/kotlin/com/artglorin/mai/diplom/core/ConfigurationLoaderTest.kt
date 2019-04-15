package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.Category
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 01/05/2018
 */
@Tag(Category.UNIT)
internal class ConfigurationLoaderTest {

    @Test
    fun `test fail when source does not exist`() {
        assertThrows(ConfigurationNotLoaded::class.java, {
            val path = TempFile(file = Paths.get("test.json"))
            load(path.file)
        })
    }

    @Test
    fun `test fail when source is exist but is not a json structure`() {
        assertThrows(ConfigurationNotLoaded::class.java, {
            val path = TempFile()
            load(path.file)
        })
    }

    @Test fun `test that APP_CONFIG file is loaded without issue when configuration file is not specified`() {
        assertNotNull(ConfigurationLoader.APP_CONFIG.loadProperties())
    }

    private fun load(path: Path) = ConfigurationLoader({ path.toUri() }).loadProperties()

    @Test
    fun `test correct read value`() {
        val configuration = Configuration(modulesPath = "/test/modules")
        val tempFile = TempFile()
        tempFile.save(configuration)
        val loadedConfiguration = load(tempFile.file)
        assertEquals(configuration, loadedConfiguration)
    }

    private data class TempFile(val file: Path = Files.createTempFile(null, ".json")) {

        fun save(it: Any) {
            ObjectMapper().writeValue(file.toFile(), it)
        }
    }
}