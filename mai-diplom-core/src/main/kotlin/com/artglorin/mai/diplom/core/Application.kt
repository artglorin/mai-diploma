package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.core.ConfigurationLoader.Companion.APP_CONFIG
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.util.*
import javax.annotation.PostConstruct

@SpringBootApplication
open class Application(@Autowired private val loader: MultipleModuleLoader) {
    companion object {
        val LOG = LoggerFactory.getLogger(Application::class.java.name)!!
    }

    @PostConstruct
    fun init() {
        LOG.debug("Starting load modules")
        val result = loader.load(arrayOf(
                DataSourceModule::class
                , TaskManagerModule::class
                , DataHandlerModule::class
                , DataObserver::class
                , SolutionModule::class
        ))
        val sources = result.getModulesFor(DataSourceModule::class, ModulesNames.DATA_SOURCES)
        val taskManager = result.getModulesFor(TaskManagerModule::class, ModulesNames.TASK_MANAGER)[0]
        val dataHandlers = result.getModulesFor(DataHandlerModule::class, ModulesNames.DATA_HANDLERS)
        val observers = result.getModulesFor(DataObserver::class, ModulesNames.DATA_OBSERVERS, false)
        val solution = result.getModulesFor(SolutionModule::class, ModulesNames.SOLUTION)[0]
        LOG.info("Modules were loaded. Starting configure modules")
        val allModules = ArrayList(sources) + taskManager + dataHandlers + observers + solution
        val configuration = APP_CONFIG.loadProperties()
        configuration.configure(allModules)
        LOG.debug("Add observers to modules")
        LOG.debug("Set sources and handlers to task manager")
        taskManager.setData(
                TaskManagerData(
                        sources,
                        dataHandlers,
                        solution,
                        observers,
                        configuration.dataFlow,
                        configuration.pipes.map { PipeFactory.create(it) }
                )
        )
        LOG.debug("Run tasks")
        taskManager.process()
    }

}