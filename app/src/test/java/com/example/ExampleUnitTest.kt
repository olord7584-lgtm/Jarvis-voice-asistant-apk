package com.example

import com.example.ai.ActionType
import com.example.ai.JarvisAiService
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun jarvisClassifiesFlashlightIntentAccurately() {
        val aiService = JarvisAiService()
        val action = aiService.classifyIntent("Jarvis, turn on the flashlight")
        assertEquals(ActionType.FLASHLIGHT_ON, action.actionType)
    }

    @Test
    fun jarvisClassifiesBatteryIntentAccurately() {
        val aiService = JarvisAiService()
        val action = aiService.classifyIntent("What is the battery level?")
        assertEquals(ActionType.CHECK_BATTERY, action.actionType)
    }

    @Test
    fun jarvisClassifiesNoteDirectiveIntent() {
        val aiService = JarvisAiService()
        val action = aiService.classifyIntent("Note: Upgrade armor plating")
        assertEquals(ActionType.SAVE_NOTE, action.actionType)
        assertEquals("Upgrade armor plating", action.parameter)
    }

    @Test
    fun jarvisClassifiesSystemDiagnosticsIntent() {
        val aiService = JarvisAiService()
        val action = aiService.classifyIntent("Run system diagnostics")
        assertEquals(ActionType.SYSTEM_DIAGNOSTICS, action.actionType)
    }
}
