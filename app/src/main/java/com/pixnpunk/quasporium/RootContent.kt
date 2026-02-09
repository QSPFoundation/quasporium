package com.pixnpunk.quasporium

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalFocusManager
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.details.presentation.DetailsContent
import com.pixnpunk.details.presentation.RealDetailsComponent
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.dialogs.presentation.DialogsState
import com.pixnpunk.dto.asString
import com.pixnpunk.dto.stringToEntry
import com.pixnpunk.quasporium.theme.QuasporiumTheme
import com.pixnpunk.settings.presentation.RealSettingsComponent
import com.pixnpunk.settings.presentation.SettingsContent
import com.pixnpunk.stock.presentation.RealStockComponent
import com.pixnpunk.stock.presentation.StockContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun RootContent(
    component: RootComponent
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()
    val settings by component.settingsState.collectAsState()

    val scope = rememberCoroutineScope()

    val scaffoldNavigator = rememberListDetailPaneScaffoldNavigator<String>()

    val focusManager = LocalFocusManager.current

    QuasporiumTheme(
        themeMode = settings.appThemeMode
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settings)
        }

        NavigableListDetailPaneScaffold(
            navigator = scaffoldNavigator,
            listPane = {
                val stockComponent = rememberRetainedComponent(key = "StockComponent") {
                    RealStockComponent(
                        componentContext = it,
                        showDialogAdd = component::doShowDialog,
                        navigateToSettings = {
                            scope.launch {
                                scaffoldNavigator.navigateTo(
                                    ListDetailPaneScaffoldRole.Extra
                                )
                                focusManager.clearFocus(force = true)
                            }
                        },
                        navigateToDetails = { inEntry ->
                            component.fetchDescConvData(
                                entry = inEntry,
                                completeScope = { entry ->
                                    scope.launch {
                                        scaffoldNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.Detail,
                                            entry.asString()
                                        )
                                        focusManager.clearFocus(force = true)
                                    }
                                }
                            )
                        }
                    )
                }
                AnimatedPane {
                    StockContent(stockComponent, settings)
                }
            },
            detailPane = {
                scaffoldNavigator.currentDestination?.contentKey?.let { entry ->
                    val detailsContent = rememberRetainedComponent(key = "DetailsComponent") {
                        RealDetailsComponent(
                            componentContext = it,
                            showSelectGameDialog = { e ->
                                component.doShowDialog(
                                    state = DialogsState.DIALOG_SELECT_RUN_GAME,
                                    entry = e
                                )
                            },
                            showEditGameDialog = { e ->
                                component.doShowDialog(
                                    state = DialogsState.DIALOG_EDIT_GAME,
                                    entry = e
                                )
                            },
                            showDownloadGameDialog = { e ->
                                component.doShowDialog(
                                    state = DialogsState.DIALOG_DOWNLOAD_GAME,
                                    entry = e
                                )
                            },
                            onDismissed = {
                                scope.launch {
                                    focusManager.clearFocus(force = true)
                                    if (scaffoldNavigator.canNavigateBack()) {
                                        scaffoldNavigator.navigateBack()
                                    } else {
                                        scaffoldNavigator.navigateTo(
                                            ListDetailPaneScaffoldRole.List
                                        )
                                    }
                                }
                            },
                        )
                    }
                    AnimatedPane {
                        DetailsContent(detailsContent, settings, entry.stringToEntry())
                    }
                }
            },
            extraPane = {
                val settingsContent = rememberRetainedComponent(key = "SettingsComponent") {
                    RealSettingsComponent(
                        componentContext = it,
                        showColorDialog = component::doShowDialog,
                        showInputDialog = component::doShowDialog,
                        onDismissed = {
                            scope.launch {
                                focusManager.clearFocus(force = true)
                                if (scaffoldNavigator.canNavigateBack()) {
                                    scaffoldNavigator.navigateBack()
                                } else {
                                    scaffoldNavigator.navigateTo(
                                        ListDetailPaneScaffoldRole.List
                                    )
                                }
                            }
                        }
                    )
                }
                AnimatedPane { SettingsContent(settingsContent) }
            }
        )
    }
}