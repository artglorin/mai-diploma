package com.artglorin.mai.diplom

import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.CoroutineExceptionHandler
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
open class Application(@Autowired private val factory: ModuleLoaderFactory) {
    companion object {
        val LOG = LoggerFactory.getLogger(Application::class.java.name)!!
    }

    @PostConstruct
    fun init() {
        var sourceModulesLoadResult: LoadResult<DataSourceModule>? = null
        var taskManagerModulesLoadResult: LoadResult<TaskManagerModule>? = null
        var dataHandlerModulesLoadResult: LoadResult<DataHandlerModule>? = null
        var dataObserversLoadResult: LoadResult<DataObserver>? = null
        var solutionModulesLoadResult: LoadResult<SolutionModule>? = null
        runBlocking(CommonPool + CoroutineExceptionHandler({ _, e ->
            {

            }

        })) {
            LOG.info("Starting load modules")
            val task1 = async {
                sourceModulesLoadResult = factory.createSourceModuleLoader().load()
            }
            val task2 = async {
                taskManagerModulesLoadResult = factory.createTaskManagerModuleLoader().load()
            }
            val task3 = async {
                dataHandlerModulesLoadResult = factory.createDataHandlerModuleLoader().load()
            }
            val task4 = async {
                dataObserversLoadResult = factory.createDataObserversLoader().load()
            }
            val task5 = async {
                solutionModulesLoadResult = factory.createSolutionModuleLoader().load()
            }
            task1.await()
            task2.await()
            task3.await()
            task4.await()
            task5.await()
        }
        val sources = getModules(sourceModulesLoadResult, ModulesNames.DATA_SOURCES)
        val taskManager = getModules(taskManagerModulesLoadResult, ModulesNames.TASK_MANAGER)[0]
        val dataHandlers = getModules(dataHandlerModulesLoadResult, ModulesNames.DATA_HANDLERS)
        val observers = getModules(dataObserversLoadResult, ModulesNames.DATA_OBSERVERS, false)
        val resolvers = getModules(solutionModulesLoadResult, ModulesNames.SOLUTION)[0]
        (ArrayList<OutputModule>(sources) + dataHandlers + resolvers).forEach {
            val observable = it
            observers.filter { it.getObservablesIds().contains(observable.getModuleId()) }.forEach(observable::addObserver)
        }
        taskManager.addSources(sources)
        taskManager.addHandlers(dataHandlers)
        taskManager.process()
    }

    private fun <T> getModules(loadResult: LoadResult<T>?, moduleName: String, required: Boolean = true): List<T> {
        if (required
                && (loadResult == null
                        || loadResult.success.not()
                        || loadResult.classes.isEmpty())) {
            throw RequiredModulesNotLoaded(loadResult?.message ?: "Modules for required module by name '$moduleName' were not loaded. ")

        }
        return loadResult?.classes ?: emptyList()
    }
}