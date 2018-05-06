package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.DataSourceModule
import com.artglorin.mai.diplom.core.Settingable
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class JsonDataSource : DataSourceModule, Settingable {
    private var sourceFile: Path? = null
    private var outputJson: JsonNode? = null

    override fun applySettings(settings: JsonNode) {
        if (settings.isNull || settings.isMissingNode || settings.size() == 0 || settings.isObject.not()) {
            throw IllegalArgumentException("Cannot configure Json data from empty or not object node settings '$settings'")
        }
        sourceFile = Paths.get(settings.path("sourceFile").asText())
        outputJson = settings.path("outputSchema")
    }


    override fun getOutputSchema(): JsonNode {
        return outputJson ?: throw IllegalStateException("OutputSchema was not specified")
    }

    override fun addObserver(observer: Consumer<JsonNode>) {
    }

    override fun getData(): Stream<JsonNode> {
        val mapper = ObjectMapper()
        return Files.lines(sourceFile).map { mapper.readTree(it) }.filter(Objects::nonNull)
    }
}
