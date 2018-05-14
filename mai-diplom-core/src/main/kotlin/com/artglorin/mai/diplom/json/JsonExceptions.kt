package com.artglorin.mai.diplom.json

import com.fasterxml.jackson.databind.JsonNode

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 07/05/2018
 */

open class JsonException(message: String): RuntimeException(message)

class JsonPathValidationException(message: String ): JsonException(message)

class JsonPathCreationException(message: String ): JsonException(message)

class CannotConvertValue(from: JsonNode, path: String, matcher: String ): JsonException("Cannot convert value in $from by path: $path with matcher: $matcher")