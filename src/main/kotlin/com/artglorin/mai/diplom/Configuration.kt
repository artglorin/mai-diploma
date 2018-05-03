package com.artglorin.mai.diplom

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 03/05/2018
 */
data class Configuration(
        var modules: Modules = Modules()
)

data class Modules (
        var path: String = FilesAndFolders.MODULES_DIR,
        val dataSources: List<ModuleConfig> = emptyList(),
        val taskManager: ModuleConfig = ModuleConfig(),
        val solutionModule: ModuleConfig = ModuleConfig(),
        val dataHandlers: List<ModuleConfig> = emptyList(),
        val dataObservers: List<ModuleConfig> = emptyList()

)

data class ModuleConfig(var jarName: String = "",
                        var settings: JsonNode = ObjectMapper().createObjectNode().nullNode(),
                        var id: String = ""
)