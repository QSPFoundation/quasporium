package com.pixnpunk.game.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.game.presentation.theme.QuasporiumTheme
import com.pixnpunk.settings.SettingsRepository

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GameContent(
    component: GameComponent,
    settingsRepository: SettingsRepository,
    onFinish: () -> Unit
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()
    val settings by settingsRepository.settingsFlow.collectAsState()

    val isScreenExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    QuasporiumTheme(
        darkTheme = when {
            settings.theme.contains("auto") -> isSystemInDarkTheme()
            settings.theme.contains("dark") -> true
            settings.theme.contains("light") -> false
            else -> false
        },
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settingsRepository)
        }

        if (isScreenExpanded) {
            GameContentExpand(
                component = component,
                onFinish = onFinish
            )
        } else {
            GameContentStandard(
                component = component,
                onFinish = onFinish
            )
        }
    }
}