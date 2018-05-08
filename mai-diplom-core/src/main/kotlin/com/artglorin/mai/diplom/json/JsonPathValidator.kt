package com.artglorin.mai.diplom.json

object JsonPathValidator {
    private val notAnIndex = Regex(".*\\[\\d*?[^]\\d]+\\d*?]?.*")

    /**
     * Validate path and return end object Type
     * @return JsonType
     */
    fun validate(path: String) {
        if (path.isBlank()) throw JsonPathValidationException("Path must not be null")
//        if (path.count { it == '=' } != 1) throw JsonPathValidationException("Path must have one equals sign. Path $path")
        if (notAnIndex.matches(path)) throw JsonPathValidationException("Only digit must be in square bracket. Path $path")
    }
}