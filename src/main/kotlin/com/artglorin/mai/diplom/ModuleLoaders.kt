package com.artglorin.mai.diplom

import org.apache.commons.lang3.StringUtils
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
    fun createDataViewModuleLoader(): ModuleLoader<DataObserver>
    fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule>
    fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule>
    fun createResolverModuleLoader(): ModuleLoader<DataResolverModule>
}

@Component
class DefaultModuleLoaderFactory: ModuleLoaderFactory {
    override fun createResolverModuleLoader(): ModuleLoader<DataResolverModule> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createSourceModuleLoader(): ModuleLoader<DataSourceModule> {
        val loadProperties = PropertiesLoader.APP_CONFIG.loadProperties()
        val modulesDir = loadProperties.path(ConfigKeys.MODULES_DIR).asText(FilesAndFolders.MODULES_DIR)
        if (StringUtils.isBlank(modulesDir) || Files.exists(Paths.get(modulesDir)).not()) {
            throw RequiredModulesNotLoaded("data-sources module is not specified")
        }
        val dataSource = Paths.get(modulesDir).resolve(FilesAndFolders.DATA_SOURCES_MODULE_DIR)
        return ModuleLoaderImpl("data-sources", dataSource, DataSourceModule::class.java)
    }

    override fun createDataViewModuleLoader(): ModuleLoader<DataObserver> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}

class ModuleLoaderImpl<out T>(private val moduleName: String,
                          private val moduleFolder: Path,
                          private val moduleClass: Class<T>) : ModuleLoader<T> {
    init {
        if (StringUtils.isBlank(moduleName)) throw IllegalArgumentException("Module name must be not blank")
        if (Files.exists(moduleFolder).not()) throw IllegalArgumentException("Folder: '$moduleFolder' for module '$moduleName' is not exist")
    }

    override fun load(): LoadResult<T> {
        try {
            Files.list(moduleFolder)
                    .filter({ it.fileName.toString().endsWith(".jar") })
                    .map { it.toUri().toURL() }
                    .collect(Collectors.toList())
                    .let {
                        val loader = URLClassLoader(it.toTypedArray())
                        val load = ServiceLoader.load(moduleClass, loader)
                        load.toList()
                    }.apply {
                        return LoadResult(true, "Modules for module by name '$moduleName' were loaded. Count of modules: ${this.size}", this)
                    }
            return LoadResult(true, "No one module was found for module name '$moduleName' in folder '$moduleFolder'", emptyList())
        } catch (ex: Throwable) {
            return LoadResult(false, ex.message?: "Module load fail with exception: $ex", emptyList())
        }
    }

}