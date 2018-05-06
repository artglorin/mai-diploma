package com.artglorin.mai.diplom

import org.slf4j.Logger

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 03/05/2018
 */

fun Logger.error(exception: (msg:String) -> Throwable, msg : String) {
    error(msg)
    throw exception.invoke(msg)
}

fun Enum<*>.toLowerCaseCollection(): Collection<String> {
    return this::class.java.enumConstants.map { it.name.toLowerCase() }

}

fun Enum<*>.toLowerCase() = name.toLowerCase()