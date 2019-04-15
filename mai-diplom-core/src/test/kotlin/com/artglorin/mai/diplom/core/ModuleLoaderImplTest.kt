package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.core.api.DataSourceModule
import com.artglorin.mai.diplom.test.JarUtil.makeServiceJarWithSingleClass
import com.fasterxml.jackson.databind.node.ObjectNode
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 02/05/2018
 */
internal class ModuleLoaderImplTest {
    private var testFolder: Path? = null
    private var configFile: Path? = null

    @BeforeEach
    fun createTestData() {
        testFolder = Files.createTempDirectory(null)
        configFile = testFolder?.resolve(FilesAndFolders.CONFIG_FILE)
        Files.createFile(configFile)
        System.setProperty("user.dir", testFolder?.toAbsolutePath().toString())
    }

    @AfterEach
    fun deleteTestData() {
        testFolder?.toFile().apply { FileUtils.forceDelete(this) }
    }

    @Test
    fun load() {
        val folder = testFolder ?: fail("test folder was not created")
        makeServiceJarWithSingleClass(folder, "mod1", EmptyDataSource::class.java)
        val loadResult: LoadResult<DataSourceModule> = runBlocking {
            ModuleLoaderImpl(DataSourceModule::class, "test-module", folder).load()
        }
        assertTrue(loadResult.success)
        assertEquals(1, loadResult.classes.size)
    }

    @Test
    fun `test load module with empty jar archive`() {
        val folder = testFolder ?: fail("test folder was not created")
        val loadResult = runBlocking {
            ModuleLoaderImpl(DataSourceModule::class, "test-module", folder).load()
        }
        assertTrue(loadResult.success)
        assertEquals(0, loadResult.classes.size)
    }

    class PrivateDataSource(val int: Int) : DataSourceModule {
        override fun getOutputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addListener(listener: Consumer<ObjectNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun launch() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

    }

    open class EmptyDataSource : DataSourceModule {
        override fun addListener(listener: Consumer<ObjectNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun launch() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getOutputSchema(): ObjectNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    @Test
    fun `test fail load module without default public constructor`() {
        val folder = testFolder ?: fail("test folder was not created")
        makeServiceJarWithSingleClass(folder, "mod1", PrivateDataSource::class.java)
        val loadResult = runBlocking {
            ModuleLoaderImpl(DataSourceModule::class, "test-module", folder).load()
        }
        assertFalse(loadResult.success)
        assertEquals(0, loadResult.classes.size)
    }

}