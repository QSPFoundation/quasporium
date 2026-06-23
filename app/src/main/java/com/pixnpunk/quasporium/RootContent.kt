package com.pixnpunk.quasporium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

    val detailsSlotIsNotEmpty = detailsSlot.child != null

    val settingsChild = settingsSlot.child?.instance
    val showSettingsInExtraPane = settingsChild != null && settingsChild.isLocalSettings
    val showSettingsAsOverlay = settingsChild != null && !settingsChild.isLocalSettings

    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())

    val scaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        maxVerticalPartitions = directive.maxVerticalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        destinationHistory = remember(detailsSlot.child, settingsSlot.child) {
            buildList {
                add(ThreePaneScaffoldDestinationItem<Any>(pane = ListDetailPaneScaffoldRole.List))

                if (detailsSlot.child != null) {
                    add(ThreePaneScaffoldDestinationItem<Any>(pane = ListDetailPaneScaffoldRole.Detail))
                }

                if (showSettingsInExtraPane) {
                    add(ThreePaneScaffoldDestinationItem<Any>(pane = ListDetailPaneScaffoldRole.Extra))
                }
            }
        }
    )

    QuasporiumTheme(
        themeMode = settings.appThemeMode
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settings)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                color = MaterialTheme.colorScheme.background
            ) {
                ListDetailPaneScaffold(
                    directive = directive,
                    value = remember(scaffoldValue, detailsSlotIsNotEmpty, showSettingsInExtraPane) {
                        ThreePaneScaffoldValue(
                            primary = if (detailsSlotIsNotEmpty) scaffoldValue.primary else PaneAdaptedValue.Hidden,
                            secondary = scaffoldValue.secondary,
                            tertiary = if (showSettingsInExtraPane) scaffoldValue.tertiary else PaneAdaptedValue.Hidden
                        )
                    },
                    listPane = {
                        AnimatedPane {
                            StockContent(component.stockComponent)
                        }
                    },
                    detailPane = {
                        AnimatedPane {
                            detailsSlot.child?.instance?.also {
                                DetailsContent(it)
                            }
                        }
                    },
                    extraPane = {
                        AnimatedPane {
                            if (showSettingsInExtraPane) { SettingsContent(settingsChild) }
                        }
                    }
                )
            }

            AnimatedVisibility(
                visible = showSettingsAsOverlay,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showSettingsAsOverlay) {
                        SettingsContent(settingsChild)
                    }
                }
            }
        }
    }
}