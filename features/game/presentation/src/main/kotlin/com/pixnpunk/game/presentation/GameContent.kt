package com.pixnpunk.game.presentation

import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.extra.presentation.ExtraContent
import com.pixnpunk.game.domain.model.GameState
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameBottomBar
import com.pixnpunk.game.presentation.model.GameUIState
import com.pixnpunk.game.presentation.theme.AppTypography
import com.pixnpunk.game.presentation.theme.QuasporiumTheme
import com.pixnpunk.game.presentation.theme.withDefaultFontFamily
import com.pixnpunk.main.presentation.MainContent
import com.pixnpunk.`object`.presentation.ObjectContent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GameContent(
    component: GameComponent,
    state: GameUIState,
    snackbarHostState: SnackbarHostState
) {
    val dialogSlot by component.dialogSlot.subscribeAsState()
    val extraSlot by component.extraSlot.subscribeAsState()
    val objectSlot by component.objectSlot.subscribeAsState()

    val settings by component.settingsState.collectAsStateWithLifecycle()

    val badges by component.badgesState.collectAsStateWithLifecycle()

    val engineState by component.engineState.collectAsStateWithLifecycle()

    val extraSlotIsNotEmpty = extraSlot.child != null
    val objectSlotIsNotEmpty = objectSlot.child != null

    val isMainDescUpdated = badges.hasNewMain
    val isExtraDescUpdated = badges.hasNewExtra
    val isObjectsListUpdated = badges.hasNewObjects
    val isActionsListUpdated = badges.hasNewActions

    val coroutineScope = rememberCoroutineScope()
    val doubleTapTimeout = LocalViewConfiguration.current.doubleTapTimeoutMillis
    var clickJob by remember { mutableStateOf<Job?>(null) }

    var showDialog by remember { mutableStateOf(false) }

    val directive = calculatePaneScaffoldDirective(
        windowAdaptiveInfo = currentWindowAdaptiveInfoV2()
    )

    val currentRole = when {
        extraSlotIsNotEmpty -> SupportingPaneScaffoldRole.Supporting
        objectSlotIsNotEmpty -> SupportingPaneScaffoldRole.Extra
        else -> SupportingPaneScaffoldRole.Main
    }

    val currentIsMain = currentRole == SupportingPaneScaffoldRole.Main

    LaunchedEffect(component) {
        component.events.collect { event ->
            when (event) {
                is GameEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    LaunchedEffect(isMainDescUpdated, currentIsMain) {
        if (currentIsMain && isMainDescUpdated) {
            component.cleanMainUpdates()
        }
    }

    LaunchedEffect(isActionsListUpdated, currentIsMain) {
        if (currentIsMain && isActionsListUpdated) {
            component.cleanActionsUpdates()
        }
    }

    LaunchedEffect(isExtraDescUpdated, extraSlotIsNotEmpty) {
        if (extraSlotIsNotEmpty && isExtraDescUpdated) {
            component.cleanExtraUpdates()
        }
    }

    LaunchedEffect(isObjectsListUpdated, objectSlotIsNotEmpty) {
        if (objectSlotIsNotEmpty && isObjectsListUpdated) {
            component.cleanObjectsUpdates()
        }
    }

    LaunchedEffect(state.isExtraEnabled, state.isObjectsEnabled) {
        if (!state.isExtraEnabled || !state.isObjectsEnabled) {
            component.clearAllSlot()
        }
    }

    BackHandler(enabled = !extraSlotIsNotEmpty && !objectSlotIsNotEmpty) { showDialog = true }

    val baseScaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        maxVerticalPartitions = directive.maxVerticalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        destinationHistory = remember(extraSlot.child, objectSlot.child) {
            buildList {
                add(ThreePaneScaffoldDestinationItem<Any>(pane = SupportingPaneScaffoldRole.Main))

                if (extraSlot.child != null) {
                    add(ThreePaneScaffoldDestinationItem<Any>(pane = SupportingPaneScaffoldRole.Supporting))
                }

                if (objectSlot.child != null) {
                    add(ThreePaneScaffoldDestinationItem<Any>(pane = SupportingPaneScaffoldRole.Extra))
                }
            }
        }
    )

    QuasporiumTheme(
        themeMode = settings.appThemeMode,
        typography = remember(
            settings.isUseCustomFont,
            settings.customFontPath
        ) {
            if (!settings.isUseCustomFont) {
                AppTypography
            } else {
                if (settings.customFontPath.isBlank()) {
                    AppTypography
                } else {
                    runCatching {
                        Typography().withDefaultFontFamily(
                            FontFamily(
                                Typeface.createFromFile(
                                    settings.customFontPath
                                )
                            )
                        )
                    }.getOrDefault(AppTypography)
                }
            }
        }
    ) {
        dialogSlot.child?.instance?.also { DialogsMainContent(it) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Exit without saving?") },
                confirmButton = {
                    Button(onClick = {
                        showDialog = false
                        component.doFinishGame()
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = {
                        showDialog = false
                    }) {
                        Text("No")
                    }
                }
            )
        }

        GameBottomBar(
            isExtraEnabled = state.isExtraEnabled,
            isObjectsEnabled = state.isObjectsEnabled,
            extraSlotIsNotEmpty = extraSlotIsNotEmpty,
            objectSlotIsNotEmpty = objectSlotIsNotEmpty,
            activateExtraSlot = component::stateSwitchExtraSlot,
            activateObjectSlot = component::stateSwitchObjectSlot,
            cleanAllSlot = component::clearAllSlot,
            mainBadge = {
                if (!currentIsMain
                    && (!extraSlotIsNotEmpty || !objectSlotIsNotEmpty)
                    && (isMainDescUpdated || isActionsListUpdated)
                ) Badge()
            },
            extraBadge = {
                if (state.isExtraEnabled
                    && !extraSlotIsNotEmpty
                    && isExtraDescUpdated
                ) Badge()
            },
            objectBadge = {
                if (state.isObjectsEnabled
                    && !objectSlotIsNotEmpty
                    && isObjectsListUpdated
                ) Badge()
            }
        ) {
            SupportingPaneScaffold(
                directive = directive,
                value = remember(baseScaffoldValue, extraSlotIsNotEmpty, objectSlotIsNotEmpty) {
                    ThreePaneScaffoldValue(
                        primary = baseScaffoldValue.primary,
                        secondary = if (extraSlotIsNotEmpty) baseScaffoldValue.secondary else PaneAdaptedValue.Hidden,
                        tertiary = if (objectSlotIsNotEmpty) baseScaffoldValue.tertiary else PaneAdaptedValue.Hidden
                    )
                },
                paneExpansionDragHandle = {
                    val interactionSource = remember { MutableInteractionSource() }
                    VerticalDragHandle(
                        modifier =
                            Modifier.paneExpansionDraggable(
                                it,
                                LocalMinimumInteractiveComponentSize.current,
                                interactionSource,
                            ),
                        interactionSource = interactionSource,
                    )
                },
                mainPane = {
                    AnimatedPane {
                        MainContent(
                            component = component.mainComponent,
                            topAppBar = {
                                GameAppBar(
                                    titleText = stringResource(R.string.main_tab_title),
                                    isInputExecutorVis = state.isInputEnabled || state.isExecutorEnabled,
                                    isAppBarMenuVis = engineState is GameState.Running,
                                    isSaveMenuItemVis = !state.isSaveDisable,
                                    showDialog = component::doShowDialog,
                                    doCloseGame = {
                                        if (clickJob?.isActive == true) {
                                            clickJob?.cancel()
                                            component.doFinishGame()
                                        } else {
                                            clickJob = coroutineScope.launch {
                                                delay(doubleTapTimeout.milliseconds)
                                                snackbarHostState.showSnackbar(
                                                    message = "Press back again to close",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    },
                                    doSaveFile = component::doSaveFile,
                                    doLoadFile = component::doLoadFile,
                                    doRestartGame = component::doRestartGame
                                )
                            },
                            snackbarHostState = snackbarHostState
                        )
                    }
                },
                supportingPane = {
                    AnimatedPane {
                        extraSlot.child?.instance?.let { child ->
                            ExtraContent(child, component::clearAllSlot)
                        }
                    }
                },
                extraPane = {
                    AnimatedPane {
                        objectSlot.child?.instance?.let { child ->
                            ObjectContent(child, component::clearAllSlot)
                        }
                    }
                }
            )
        }
    }
}