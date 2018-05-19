package com.artglorin.mai.diplom.synoptic.modules.datalisteners

import com.artglorin.mai.diplom.core.api.Customizable
import com.artglorin.mai.diplom.core.api.DataObserver
import com.artglorin.mai.diplom.json.JsonSchemaBuilder
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.io.File

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 18/05/2018
 */

class FileOutputModule: Customizable, DataObserver {
    private var file: File? = null
    override fun getInputSchema(): ObjectNode {
        return JsonSchemaBuilder().build()
    }

    override fun push(node: ObjectNode) {
        file?.appendText("$node\n")
    }

    override fun applySettings(settings: JsonNode) {
        file = settings.get("file").textValue()?.let {
            File(it)
        }?.apply {
            createNewFile()
        }
    }
}

class ConsoleOutputModule: DataObserver {
    override fun getInputSchema(): ObjectNode {
        return JsonSchemaBuilder().build()
    }

    override fun push(node: ObjectNode) {
        println(node)
    }
}