package com.artglorin.mai.diplom

import com.artglorin.mai.diplom.core.Application
import com.artglorin.mai.diplom.core.DefaultModuleLoaderFactory
import com.artglorin.mai.diplom.core.MultiplyModuleLoaderImpl

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */

fun main(args: Array<String>) {
    Application(MultiplyModuleLoaderImpl(DefaultModuleLoaderFactory())).loadModulesAndStart()
}