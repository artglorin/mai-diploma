package com.artglorin.mai.diplom

import com.artglorin.mai.diplom.test.JarUtil.makeServiceJarWithSingleClass
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.fail
import org.springframework.boot.test.context.TestConfiguration
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

@TestConfiguration
internal open class EmptyConfiguration


//@ExtendWith(SpringExtension::class)
//@ContextConfiguration(classes = [EmptyConfiguration::class])
//@SpringBootTest()
internal class LoadModulesTest {

    private var testFolder: Path ? = null
    private var configFile: Path ? = null

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
    fun `test Exception when moduleFolder property set as empty String`() {
        val conf = configFile ?: fail("config file have not been created")
        assertThrows(RequiredModulesNotLoaded::class.java,
                {
                    TestData().apply {
                        node.put(FilesAndFolders.MODULES_DIR, "")
                        save(conf)
                    }
                    Application().init()
                }
        )
    }

    @Test
    fun `test Exception when modulesFolder does not exist`() {
        assertThrows(RequiredModulesNotLoaded::class.java,
                {
                    TestData()
                    Application().init()
                }
        )
    }

    open class EmptyDataSource : DataSourceModule {
        override fun getOutputSchema(): JsonNode {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addObserver(observer: Consumer<JsonNode>) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getData(): Stream<JsonNode> {
            return Stream.empty()
        }
    }

    @Test
    fun `test success load source folder`() {
        val folder = testFolder ?: fail("test folder not created")
        val configFile = configFile ?: fail("config file not created")
        TestData().apply {
            val modulesFolder = folder.resolve(FilesAndFolders.MODULES_DIR)

            node.put(ConfigKeys.MODULES_DIR, modulesFolder.toAbsolutePath().toString())
            save(configFile)
            val dataSourceDir = modulesFolder.resolve(FilesAndFolders.DATA_SOURCES_MODULE_DIR)
            Files.createDirectories(dataSourceDir)
            makeServiceJarWithSingleClass(dataSourceDir, "mod1", EmptyDataSource::class.java)
        }
        Application().init()
    }


    private data class TestData(
                                val node: ObjectNode = ObjectMapper().createObjectNode()
    ) {

        fun save(file: Path) {
            ObjectMapper().writeValue(file.toFile(), node)
        }

    }
}