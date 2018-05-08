package com.artglorin.mai.diplom.json

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 07/05/2018
 */

open class JsonException(message: String): RuntimeException(message)

class JsonPathValidationException(message: String ): JsonException(message)

class JsonPathCreationException(message: String ): JsonException(message)