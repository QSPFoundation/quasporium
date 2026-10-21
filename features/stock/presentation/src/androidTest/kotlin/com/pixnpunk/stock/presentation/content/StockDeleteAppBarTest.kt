package com.pixnpunk.stock.presentation.content

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StockDeleteAppBarTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun buttonsHaveDescriptions() {
        rule.setContent {
            MaterialTheme {
                StockDeleteAppBar(
                    deleteSetSize = 2,
                    stopDeleteMode = {},
                    selectAll = {},
                    deleteEntries = {}
                )
            }
        }

        val descriptions = rule.onAllNodes(hasClickAction()).fetchSemanticsNodes().map {
            it.config.getOrNull(SemanticsProperties.ContentDescription).orEmpty().joinToString()
        }
        assertEquals(3, descriptions.size)
        assertEquals(emptyList<String>(), descriptions.filter { it.isBlank() })
    }
}
