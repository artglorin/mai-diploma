package com.artglorin.mai.diplom

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

data class LoadResult<out T>(val success: Boolean, val message: String, val classes: List<T>)


interface ModuleLoader<out T> {
    fun load(): LoadResult<T>
}

interface ModuleLoaderFactory {
    fun createSourceModuleLoader(): ModuleLoader<DataSourceModule>
    fun createDataObserversLoader(): ModuleLoader<DataObserver>
    fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule>
    fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule>
    fun createSolutionModuleLoader(): ModuleLoader<SolutionModule>
}

@Component
open class DefaultModuleLoaderFactory : ModuleLoaderFactory {
    companion object {
        val LOG = LoggerFactory.getLogger(Application::class.java.name)!!
    }
    private val modulesDir: Path

    init {
        val loadProperties = ConfigurationLoader.APP_CONFIG.loadProperties()
        try {
            modulesDir = Paths.get(loadProperties.modules.path)
        } catch (e: Exception) {
            throw IllegalArgumentException()
        }
        if (Files.exists(modulesDir).not()) {
            LOG.error(::IllegalArgumentException, "Modules directory does not exist. Application cannot be started. Specified directory: '$modulesDir'")
        }
    }

    override fun createSolutionModuleLoader(): ModuleLoader<SolutionModule> {
        return moduleLoaderImpl(SolutionModule::class.java, FilesAndFolders.SOLUTION_MODULE_DIR, "solution")
    }

    override fun createSourceModuleLoader(): ModuleLoader<DataSourceModule> {
        return moduleLoaderImpl(DataSourceModule::class.java, FilesAndFolders.DATA_SOURCES_MODULE_DIR, "data-sources")
    }

    override fun createDataObserversLoader(): ModuleLoader<DataObserver> {
        return moduleLoaderImpl(DataObserver::class.java, FilesAndFolders.DATA_OBSERVERS_DIR, "data-observers")

    }

    override fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule> {
        return moduleLoaderImpl(TaskManagerModule::class.java, FilesAndFolders.TASK_MANAGER_MODULE_DIR, "task-manger")
    }

    override fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule> {
        return moduleLoaderImpl(DataHandlerModule::class.java, FilesAndFolders.DATA_HANDLERS_DIR, "data-handlers")
    }

    private fun <T> moduleLoaderImpl(clazz: Class<T>, pathToModule: String, moduleName: String): ModuleLoaderImpl<T> {
        LOG.info("Create loader for modules: $moduleName")
        val dataSource = modulesDir.resolve(pathToModule)
        return ModuleLoaderImpl(moduleName, dataSource, clazz)
    }

}

class ModuleLoaderImpl<out T>(private val moduleName: String,
                              private val moduleFolder: Path,
                              private val moduleClass: Class<T>) : ModuleLoader<T> {

    companion object {
        val LOG = LoggerFactory.getLogger(Application::class.java.name)!!
    }

    init {
        if (StringUtils.isBlank(moduleName)) throw IllegalArgumentException("Module name must be not blank")
        if (Files.exists(moduleFolder).not()) throw IllegalArgumentException("Folder: '$moduleFolder' for module '$moduleName' is not exist")
    }

    override fun load(): LoadResult<T> {
        try {
            LOG.info("Starting load modules by name '$moduleName' in folder '$moduleFolder'")
            Files.list(moduleFolder)
                    .filter({ it.fileName.toString().endsWith(".jar") })
                    .map { it.toUri().toURL() }
                    .collect(Collectors.toList())
                    .let {
                        val loader = URLClassLoader(it.toTypedArray())
                        val load = ServiceLoader.load(moduleClass, loader)
                        load.toList()
                    }.apply {
                        val msg = "Modules for module by name '$moduleName' were loaded. Count of modules: ${this.size}"
                        LOG.info(msg)
                        return LoadResult(true, msg, this)
                    }
            val msg = "No one module was found for module name '$moduleName' in folder '$moduleFolder'"
            LOG.warn(msg)
            return LoadResult(true, msg, emptyList())
        } catch (ex: Throwable) {
            val msg = "Module load fail with exception: $ex"
            LOG.error(msg)
            return LoadResult(false, ex.message ?: msg, emptyList())
        }
    }

}