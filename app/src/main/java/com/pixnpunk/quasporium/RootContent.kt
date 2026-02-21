package com.pixnpunk.quasporium

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.details.presentation.DetailsContent
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.quasporium.theme.QuasporiumTheme
import com.pixnpunk.settings.presentation.SettingsContent
import com.pixnpunk.stock.presentation.StockContent

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RootContent(
    component: RootComponent
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()
    val settings by component.settingsState.collectAsState()

    val detailsSlot by component.detailsSlot.subscribeAsState()
    val settingsSlot by component.settingsSlot.subscribeAsState()

    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())
    val scaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        destinationHistory = listOf(
            ThreePaneScaffoldDestinationItem<Any>(
                pane = when {
                    settingsSlot.child != null -> ListDetailPaneScaffoldRole.Extra
                    detailsSlot.child != null -> ListDetailPaneScaffoldRole.Detail
                    else -> ListDetailPaneScaffoldRole.List
                }
            )
        )
    )

    QuasporiumTheme(
        themeMode = settings.appThemeMode
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settings)
        }

        ListDetailPaneScaffold(
            directive = directive,
            value = scaffoldValue,
            listPane = { AnimatedPane { StockContent(component.stockComponent) } },
            detailPane = {
                val child = detailsSlot.child?.instance
                if (child != null) {
                    AnimatedPane { DetailsContent(child) }
                }
            },
            extraPane = {
                val child = settingsSlot.child?.instance
                if (child != null) {
                    AnimatedPane { SettingsContent(child) }
                }
            }
        )
    }
}