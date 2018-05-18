package com.artglorin.mai.diplom.synoptic

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.nhaarman.mockito_kotlin.*
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.core.io.ClassPathResource
import java.util.function.Consumer

/**
 * @author V.Verminskiy (vverminskiy@alfabank.ru)
 * @since 17/05/2018
 */
internal class WeightsSolutionModuleTest{


    @TestFactory
    fun `test weights`(): Collection<DynamicTest> {
        val testData = ObjectMapper().readTree(ClassPathResource("WeightsSolutionModuleTestData.json").url)
        val solutionModule = WeightsSolutionModule()
        solutionModule.applySettings(testData.get("settings"))
        return (testData.get("data") as ArrayNode)
                .map {
                    DynamicTest.dynamicTest(it.get("caseName").textValue(), {
                        val listener: Consumer<ObjectNode> = mock()
                        solutionModule.addListener(listener)
                        solutionModule.push(it.get("items").map { it as ObjectNode }.toList())
                        verify(listener, times(1)).accept(eq(it.get("expected") as ObjectNode))
                    })
                }
    }


    @Test
    fun `test  weight solution`() {
        val solutionModule = WeightsSolutionModule()
        val factory = JsonNodeFactory.instance
        solutionModule.applySettings(factory.objectNode().apply {
             putArray("weights").apply {
                 addObject().put("moduleId", "one").put("weight", "0.2")
                 addObject().put("moduleId", "two").put("weight", "0.25")
                 addObject().put("moduleId", "three").put("weight", "0.25")
                 addObject().put("moduleId", "four").put("weight", "0.3")
             }
        }
        )

        val listener: Consumer<ObjectNode> = mock()
        solutionModule.addListener(listener)
        val oneAnswer = factory.objectNode().apply {
            put("moduleId", "one")
            putObject("data").put("hello", "world")
        }
        val twoAnswer = factory.objectNode().apply {
            put("moduleId", "two")
            putObject("data").put("hello", "world")
        }
        val threeAnswer = factory.objectNode().apply {
            put("moduleId", "three")
            putObject("data").put("hello", "world")
        }
        val fourAnswer = factory.objectNode().apply {
            put("moduleId", "four")
            putObject("data").put("hello", "world")
        }
        solutionModule.push(listOf(
                oneAnswer
        ))
        verify(listener, times(1)).accept(same(oneAnswer))

        solutionModule.push(listOf(
                oneAnswer
        ))


    }

}