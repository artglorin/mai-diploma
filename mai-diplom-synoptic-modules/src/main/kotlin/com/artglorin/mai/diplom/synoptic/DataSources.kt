package com.artglorin.mai.diplom.synoptic

import com.artglorin.mai.diplom.allNotNull
import com.artglorin.mai.diplom.core.DataSourceModule
import com.artglorin.mai.diplom.core.JsonNodeObservable
import com.artglorin.mai.diplom.core.JsonNodeObservableImpl
import com.artglorin.mai.diplom.core.Settingable
import com.artglorin.mai.diplom.json.*
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer
import java.util.stream.Stream

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 05/05/2018
 */

class JsonDataSource : DataSourceModule, Settingable, JsonNodeObservable {
    private var sourceFile: Path? = null
    private var filters: MutableList<Filter> = ArrayList()
    private var mappers: MutableList<Mapper> = ArrayList()
    private val factory = JsonNodeFactory.instance
    private val listeners = lazy {
        JsonNodeObservableImpl()
    }

    override fun applySettings(settings: JsonNode) {
        sourceFile = AppJsonMappers.ignoreUnknown.treeToValue(settings, Settings::class.java).apply {
            mapFilters(this)
            mapMappers(this)
        }?.sourceFile?.let { ClassPathResource(it).file.toPath() }
    }

    private fun mapMappers(settings: Settings) {
        settings.mappers?.filter {
            allNotNull(it.getter, it.setter)
        }?.map {
            Mapper(JsonFieldGetterFactory.create(it.getter!!), JsonFieldSetterFactory.create(it.setter!!))
        }?.apply { mappers.addAll(this) }
    }

    private fun mapFilters(settings: Settings) {
        settings.filters?.equals?.filter {
            allNotNull(it.field, it.value)
        }?.map {
            EqualsFilter(JsonNodeGetterFactory.create(it.field!!), it.value!!)
        }?.apply { filters.addAll(this) }
    }

    override fun addObserver(observer: Consumer<JsonNode>) {
        listeners.value.addObserver(observer)
    }

    override fun getData(): Stream<JsonNode> {
        val mapper = ObjectMapper()
        return Files.lines(sourceFile)
                .map { mapper.readTree(it) }
                .filter(Objects::nonNull).filter {
                    val node = it
                    filters.all { it.pass(node) }
                }.map {
                    val result = factory.objectNode()
                    result.put("moduleId", getModuleId())

                    val source = it
                    return@map (if (mappers.isNotEmpty()) {
                        val remapNpode = factory.objectNode()
                        mappers.forEach { it.map(source, remapNpode) }
                        remapNpode
                    } else it).let {
                        result.set("result", it)
                    }
                }.peek {
                    listeners.value.notify(it)
                }
    }

    class EqualsFilterData(var field: String? = null, var value: String? = null) : Filter() {

        override fun pass(node: JsonNode): Boolean {
            val path = field ?: return true
            val target = value ?: return true
            return JsonNodeGetter.get(path, node).let {
                it.isTextual.and(target == it.asText())
            }
        }
    }

    class EqualsFilter(private val getter: JsonNodeGetter, private val example: String) : Filter() {

        override fun pass(node: JsonNode): Boolean {
            return getter.extract(node).let {
                it.isTextual.and(example == it.asText())
            }
        }
    }

    abstract class Filter {
        abstract fun pass(node: JsonNode): Boolean
    }

    class Settings(
            var sourceFile: String? = null,
            var filters: Filters? = null,
            var mappers: List<MapperData>? = null
    )

    class Filters(var equals: List<EqualsFilterData>? = null)

    class MapperData(var getter: String? = null, var setter: String? = null)

    private data class Mapper(var getter: JsonFieldGetter, var setter: JsonFieldSetter) {
        fun map(source: JsonNode, target: JsonNode) {
            setter.set(getter.extract(source), target)
        }
    }
}
