package com.pixnpunk.game.presentation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.game.presentation.theme.QuasporiumTheme

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GameContent(
    component: GameComponent,
    settings: SettingsEntity,
    onFinish: () -> Unit
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()

    val isScreenExpanded = currentWindowAdaptiveInfo().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    QuasporiumTheme(
        themeMode = settings.appThemeMode
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settings)
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