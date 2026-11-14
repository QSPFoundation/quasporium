package com.pixnpunk.details.presentation.content.download

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pixnpunk.details.presentation.R
import com.pixnpunk.stock.domain.model.InstallState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetailsDownloadButtonsTest {
    @get:Rule
    val rule = createComposeRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val state = mutableStateOf<InstallState>(InstallState.NotStarted)
    private val calls = mutableListOf<String>()

    private fun setContent() =
        rule.setContent {
            MaterialTheme {
                DetailsDownloadButtons(
                    installState = state.value,
                    onDownload = { calls += "download" },
                    onCancel = { calls += "cancel" }
                )
            }
        }

    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun downloadStartsTheInstallation() {
        setContent()

        rule.onNodeWithText(text(R.string.download_button_description)).performClick()

        assertEquals(listOf("download"), calls)
    }

    @Test
    fun runningInstallationShowsItsStepAndCanBeCancelled() {
        setContent()

        state.value = InstallState.Downloading(0.42f)
        rule.onNodeWithText(text(R.string.details_install_downloading, 42)).assertIsDisplayed()

        state.value = InstallState.Unpacking(0.5f)
        rule.onNodeWithText(text(R.string.details_install_unpacking, 50)).assertIsDisplayed()

        state.value = InstallState.Paused
        rule.onNodeWithText(text(R.string.details_install_paused)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.details_install_cancel)).performClick()

        assertEquals(listOf("cancel"), calls)
    }

    @Test
    fun failedInstallationCanBeRetried() {
        state.value = InstallState.Failed
        setContent()

        rule.onNodeWithText(text(R.string.details_install_failed)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.details_install_retry)).performClick()

        assertEquals(listOf("download"), calls)
    }

    @Test
    fun installedGameHasNoDownloadButton() {
        state.value = InstallState.Installed
        setContent()

        rule.onNodeWithText(text(R.string.details_install_installed)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.download_button_description)).assertDoesNotExist()
    }
}
