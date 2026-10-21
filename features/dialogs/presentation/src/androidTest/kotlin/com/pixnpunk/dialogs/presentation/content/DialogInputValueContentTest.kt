package com.pixnpunk.dialogs.presentation.content

import android.content.Context
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.dialogs.presentation.R
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DialogInputValueContentTest {
    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun typedValueSurvivesRecreation() {
        var submitted: String? = null
        val restorationTester = StateRestorationTester(rule)
        restorationTester.setContent {
            MaterialTheme {
                DialogInputValueContent(
                    isError = false,
                    initialValue = "",
                    supportValue = "",
                    supportErrorValue = "",
                    keyboardOptions = KeyboardOptions.Default,
                    label = { Text("Value") },
                    inputValue = { submitted = it },
                    cleanError = {},
                    onDismiss = {}
                )
            }
        }

        rule.onNode(hasSetTextAction()).performTextInput("hello")
        restorationTester.emulateSavedInstanceStateRestore()

        rule.onNode(hasSetTextAction()).assert(hasText("hello"))
        rule.onNodeWithText(context.getString(R.string.input_button_description)).performClick()
        assertEquals("hello", submitted)
    }
}
