package com.artglorin.mai.diplom.core

/**
 * @author V.Verminskiy (develop@artglorin.com)
 * @since 01/05/2018
 */


class RequiredModulesNotLoaded(message: String? = null) : ApplicationCannotBeStarted(message)

class ConfigurationNotLoaded(message: String? = null) : ApplicationCannotBeStarted(message)

open class ApplicationCannotBeStarted(message: String? = null) : RuntimeException(message)