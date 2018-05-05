package com.artglorin.mai.diplom

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 01/05/2018
 */


class RequiredModulesNotLoaded(message: String? = null) : ApplicationCannotBeStarted(message)

class ConfigurationNotLoaded(message: String? = null) : ApplicationCannotBeStarted(message)

open class ApplicationCannotBeStarted(message: String? = null) : RuntimeException(message)