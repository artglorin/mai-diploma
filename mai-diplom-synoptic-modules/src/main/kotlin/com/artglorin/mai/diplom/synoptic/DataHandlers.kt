package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.core.DataHandlerModule
import com.artglorin.mai.diplom.core.DataMapper
import com.fasterxml.jackson.databind.JsonNode
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class Optimist: DataHandlerModule {

    override fun getDataMapper(): DataMapper {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOutputSchema(): JsonNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addObserver(observer: Consumer<JsonNode>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getInputSchema(): JsonNode {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}