package com.pixnpunk.game.presentation

import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameBackHandlerTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var panelOpen by mutableStateOf(false)
    private var exitRequests = 0

    private val panelCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            panelOpen = false
            isEnabled = false
        }
    }

    private fun openPanel() {
        rule.runOnUiThread {
            panelOpen = true
            panelCallback.isEnabled = true
        }
        rule.waitForIdle()
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Before
    fun setUp() {
        rule.runOnUiThread {
            rule.activity.onBackPressedDispatcher.addCallback(rule.activity, panelCallback)
        }
        rule.setContent {
            GameBackHandler(panelsOpen = panelOpen) { exitRequests++ }
        }
    }

    @Test
    fun backClosesAnOpenPanelWithoutAskingToExit() {
        openPanel()

        pressBack()

        assertFalse(panelOpen)
        assertEquals(0, exitRequests)
    }

    @Test
    fun backAsksToExitOnceThePanelIsClosed() {
        openPanel()

        pressBack()
        pressBack()

        assertEquals(1, exitRequests)
    }

    @Test
    fun backAsksToExitWithoutPanels() {
        pressBack()

        assertEquals(1, exitRequests)
    }
}
