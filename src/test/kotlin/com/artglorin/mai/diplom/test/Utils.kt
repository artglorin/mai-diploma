package com.artglorin.mai.diplom.test

import org.apache.commons.io.IOUtils
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 02/05/2018
 */

object JarUtil {

    fun makeServiceJarWithSingleClass(dataSourceDir: Path, jarFileName: String, sourceClass: Class<out Any>) {
        FileOutputStream(dataSourceDir.resolve("$jarFileName.jar").toFile()).use {
            JarOutputStream(it, Manifest()).use {
                val jarArch = it
                val cl = sourceClass.classLoader
                val classPath = sourceClass.name
                val classFile = classPath.replace(".", "/") + ".class"
                sourceClass.interfaces?.forEach {
                    val serviceFile = JarEntry("META-INF/services/${it.name}")
                    jarArch.putNextEntry(serviceFile)
                    jarArch.write(classPath.toByteArray())
                }
                jarArch.putNextEntry(JarEntry(classFile))
                val resource = cl.getResource(classFile)
                IOUtils.toByteArray(resource).apply {
                    it.write(this)
                }
                jarArch.closeEntry()
            }
        }
    }

}