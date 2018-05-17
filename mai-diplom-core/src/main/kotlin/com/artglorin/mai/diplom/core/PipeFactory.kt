package com.artglorin.mai.diplom.core

import com.artglorin.mai.diplom.json.JsonFilterFactory
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
                    config.id,
                    config.filter?.let { JsonFilterFactory.create(it as ObjectNode) },
                    config.template,
                    config.converters.map { JsonValueConverterFactory.create(it) }
            )
        }
    }
}
