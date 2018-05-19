package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.core.api.*
import com.artglorin.mai.diplom.error
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import java.net.URLClassLoader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import kotlin.reflect.KClass

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

data class LoadResult<out T : Module>(val clazz: KClass<out T>, val success: Boolean, val message: String, val classes: List<T>)


interface ModuleLoader<out T : Module> {
    suspend fun load(): LoadResult<T>
}

interface ModuleLoaderFactory {
    fun createLoader(module: KClass<out Module>): ModuleLoader<Module>
    fun createSourceModuleLoader(): ModuleLoader<DataSourceModule>
    fun createDataObserversLoader(): ModuleLoader<DataObserver>
    fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule>
    fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule>
    fun createSolutionModuleLoader(): ModuleLoader<SolutionModule>
}

interface MultipleModuleLoader {
    fun load(loaders: Array<KClass<out Module>>): MultipleModuleLoadResult
}

class MultipleModuleLoadResult(private val result: List<LoadResult<Module>>) {

    fun <T : Module> getModulesFor(klass: KClass<T>, moduleName: String = "UNDEFINED", required: Boolean = true): List<T> {
        val loadResult = result.find { it -> (it.clazz == klass) }
        if (required
                && (loadResult == null || loadResult.success.not().or(loadResult.classes.isEmpty()))) {
            throw RequiredModulesNotLoaded(loadResult?.message
                    ?: "Modules for required module by name '$moduleName' were not loaded. ")

        }
        @Suppress("UNCHECKED_CAST")
        return if (loadResult == null) emptyList() else loadResult.classes as List<T>
    }
}

class MultiplyModuleLoaderImpl(private val factory: ModuleLoaderFactory) : MultipleModuleLoader {
    override fun load(loaders: Array<KClass<out Module>>): MultipleModuleLoadResult {
        val factories = loaders.map(factory::createLoader)
        return MultipleModuleLoadResult(runBlocking {
            factories.map {
                async { it.load() }
            }.map { it -> it.join(); it.getCompleted() }
        }.toList())
    }
}


open class DefaultModuleLoaderFactory : ModuleLoaderFactory {
    override fun  createLoader(module: KClass<out Module>): ModuleLoader<Module> = when (module) {
        SolutionModule::class -> createSolutionModuleLoader()
        DataObserver::class -> createDataObserversLoader()
        DataHandlerModule::class -> createDataHandlerModuleLoader()
        TaskManagerModule::class -> createTaskManagerModuleLoader()
        DataSourceModule::class -> createSourceModuleLoader()
        else -> {
            throw IllegalArgumentException("Factory does not support module for class '$module'")
        }
    }

    companion object {
        val LOG = LoggerFactory.getLogger(DefaultModuleLoaderFactory::class.java.name)!!
    }

    private val modulesDir: Path

    init {
        val loadProperties = ConfigurationLoader.APP_CONFIG.loadProperties()
        try {
            val stringPath = loadProperties.modulesPath
            var path = Paths.get(stringPath)
            if (Files.exists(path).not()) {
                val classPathResource = ClassPathResource(stringPath)
                if (classPathResource.exists()) {
                    path = Paths.get(classPathResource.uri)
                } else {
                    LOG.error(::IllegalArgumentException, "Modules directory does not exist. Application cannot be started. Specified directory: '$stringPath'")

                }
            }
            modulesDir = path
        } catch (e: Exception) {
            throw IllegalArgumentException(e)
        }
        if (Files.exists(modulesDir).not()) {
        }
    }

    override fun createSolutionModuleLoader(): ModuleLoader<SolutionModule> {
        return create(SolutionModule::class.java, FilesAndFolders.SOLUTION_MODULE_DIR, ModulesNames.SOLUTION)
    }

    override fun createSourceModuleLoader(): ModuleLoader<DataSourceModule> {
        return create(DataSourceModule::class.java, FilesAndFolders.DATA_SOURCES_MODULE_DIR, ModulesNames.DATA_SOURCES)
    }

    override fun createDataObserversLoader(): ModuleLoader<DataObserver> {
        return create(DataObserver::class.java, FilesAndFolders.DATA_OBSERVERS_DIR, ModulesNames.DATA_OBSERVERS)

    }

    override fun createTaskManagerModuleLoader(): ModuleLoader<TaskManagerModule> {
        return create(TaskManagerModule::class.java, FilesAndFolders.TASK_MANAGER_MODULE_DIR, ModulesNames.TASK_MANAGER)
    }

    override fun createDataHandlerModuleLoader(): ModuleLoader<DataHandlerModule> {
        return create(DataHandlerModule::class.java, FilesAndFolders.DATA_HANDLERS_DIR, ModulesNames.DATA_HANDLERS)
    }

    private fun <T : Module> create(clazz: Class<T>, pathToModule: String, moduleName: String): ModuleLoaderImpl<T> {
        LOG.info("Create loader for modules: $moduleName")
        val dataSource = modulesDir.resolve(pathToModule)
        return ModuleLoaderImpl(clazz.kotlin, moduleName, dataSource)
    }

}

class ModuleLoaderImpl<out T : Module>(
        private val clazz: KClass<T>,
        private val moduleName: String,
        private val moduleFolder: Path) : ModuleLoader<T> {

    companion object {
        val LOG = LoggerFactory.getLogger(ModuleLoaderImpl::class.java.name)!!
    }

    init {
        if (StringUtils.isBlank(moduleName)) throw IllegalArgumentException("Module name must be not blank")
        if (Files.exists(moduleFolder).not()) throw IllegalArgumentException("Folder: '$moduleFolder' for module '$moduleName' is not exist")
    }

    override suspend fun load(): LoadResult<T> {
        try {
            LOG.info("Starting load modules by name '$moduleName' in folder '$moduleFolder'")
            val modules = Files.list(moduleFolder)
                    .filter({ it.fileName.toString().endsWith(".jar") })
                    .map { it.toUri().toURL() }
                    .collect(Collectors.toList()).toTypedArray()
                    .let {
                        val loader = URLClassLoader(it, ModuleLoaderImpl::class.java.classLoader)
                        val load = ServiceLoader.load(clazz.java, loader)
                        load.toList()
                    }
            return LoadResult(clazz,
                    true,
                    if (modules.isNotEmpty()) {
                        val msg = "Modules for module by name '$moduleName' were loaded. Count of modules: ${modules.size}, modulesIds:${modules.joinToString(",") { it.getModuleId() }}"
                        LOG.info(msg)
                        msg
                    } else {
                        val msg = "No one module was found for module name '$moduleName' in folder '$moduleFolder'"
                        LOG.warn(msg)
                        msg
                    },
                    modules)
        } catch (ex: Throwable) {
            val msg = "Module load fail with exception: $ex"
            LOG.error(msg)
            return LoadResult(clazz, false, ex.message ?: msg, emptyList())
        }
    }

}