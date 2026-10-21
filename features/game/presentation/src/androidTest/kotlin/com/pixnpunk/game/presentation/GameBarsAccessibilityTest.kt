package com.pixnpunk.game.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameBottomBar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameBarsAccessibilityTest {
    @get:Rule
    val rule = createComposeRule()

    private fun assertClickablesAreLabelled(expectedCount: Int) {
        val clickables = rule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertEquals(expectedCount, clickables.size)

        val unlabelled = clickables.filter { node ->
            val description = node.config.getOrNull(SemanticsProperties.ContentDescription)
            val text = node.config.getOrNull(SemanticsProperties.Text)
            description.orEmpty().all { it.isBlank() } && text.orEmpty().all { it.isBlank() }
        }
        assertTrue("Unlabelled: $unlabelled", unlabelled.isEmpty())
    }

    @Test
    fun appBarButtonsHaveDescriptions() {
        rule.setContent {
            MaterialTheme {
                GameAppBar(
                    titleText = "Game",
                    isInputExecutorVis = true,
                    isAppBarMenuVis = true,
                    isSaveMenuItemVis = true,
                    showDialog = {},
                    doCloseGame = {},
                    doSaveFile = {},
                    doLoadFile = {},
                    doRestartGame = {}
                )
            }
        }

        assertClickablesAreLabelled(expectedCount = 5)
    }

    @Test
    fun bottomBarItemsHaveLabels() {
        rule.setContent {
            MaterialTheme {
                GameBottomBar(
                    isExtraEnabled = true,
                    isObjectsEnabled = true,
                    extraSlotIsNotEmpty = false,
                    objectSlotIsNotEmpty = false,
                    activateExtraSlot = {},
                    activateObjectSlot = {},
                    cleanAllSlot = {},
                    content = { Text("Content") }
                )
            }
        }

        assertClickablesAreLabelled(expectedCount = 3)
    }
}
