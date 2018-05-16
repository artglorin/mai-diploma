package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.json.JsonFilterFactory
import com.artglorin.mai.diplom.json.JsonNodeCopierFactory
import com.artglorin.mai.diplom.json.JsonValueConverterFactory
import com.fasterxml.jackson.databind.node.ObjectNode

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 16/05/2018
 */
class PipeFactory {
    companion object {
        fun create(config: PipeConfiguration): Pipe {
            return Pipe(
                    config.inputId,
                    config.outputId,
                    config.filters.map { JsonFilterFactory.create(it as ObjectNode) },
                    config.copiers.map { JsonNodeCopierFactory.create(it) },
                    config.converters.map { JsonValueConverterFactory.create(it) }
            )
        }
    }
}