package com.pixnpunk.game.presentation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Badge
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.extra.presentation.ExtraContent
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameBottomBar
import com.pixnpunk.game.presentation.theme.QuasporiumTheme
import com.pixnpunk.main.presentation.MainContent
import com.pixnpunk.`object`.presentation.ObjectContent

@OptIn(ExperimentalDecomposeApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GameContent(
    component: GameComponent,
    settings: SettingsEntity,
    onFinish: () -> Unit
) {
    val state by component.model.collectAsStateWithLifecycle()
    val dialogSlot by component.dialogSlot.subscribeAsState()
    val extraSlot by component.extraSlot.subscribeAsState()
    val objectSlot by component.objectSlot.subscribeAsState()

    val extraSlotIsNotEmpty = extraSlot.child != null
    val objectSlotIsNotEmpty = objectSlot.child != null

    val unreadMainCount = remember(state.countMainPaneUpdates) {
        state.countMainPaneUpdates.count { !it.value }
    }
    val unreadExtraCount = remember(state.countExtraPaneUpdates) {
        state.countExtraPaneUpdates.count { !it.value }
    }
    val unreadObjectsCount = remember(state.countObjectsPaneUpdates) {
        state.countObjectsPaneUpdates.count { !it.value }
    }

    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())

    val currentRole = when {
        extraSlotIsNotEmpty -> SupportingPaneScaffoldRole.Supporting
        objectSlotIsNotEmpty -> SupportingPaneScaffoldRole.Extra
        else -> SupportingPaneScaffoldRole.Main
    }

    val destinationHistory = remember(currentRole) {
        listOf(ThreePaneScaffoldDestinationItem<Any>(pane = currentRole))
    }

    val currentIsMain = currentRole == SupportingPaneScaffoldRole.Main

    LaunchedEffect(state.countMainPaneUpdates, currentIsMain) {
        if (currentIsMain && unreadMainCount > 0) {
            component.cleanCountUpdates()
        }
    }

    val baseScaffoldValue = calculateThreePaneScaffoldValue(
        maxHorizontalPartitions = directive.maxHorizontalPartitions,
        maxVerticalPartitions = directive.maxVerticalPartitions,
        adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
        destinationHistory = destinationHistory
    )

    val correctedValue = remember(baseScaffoldValue, extraSlotIsNotEmpty, objectSlotIsNotEmpty) {
        ThreePaneScaffoldValue(
            primary = baseScaffoldValue.primary,
            secondary = if (extraSlotIsNotEmpty) baseScaffoldValue.secondary else PaneAdaptedValue.Hidden,
            tertiary = if (objectSlotIsNotEmpty) baseScaffoldValue.tertiary else PaneAdaptedValue.Hidden
        )
    }

    QuasporiumTheme(
        themeMode = settings.appThemeMode
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settings)
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
                if ((!extraSlotIsNotEmpty || !objectSlotIsNotEmpty) && !currentIsMain && unreadMainCount > 0) Badge()
            },
            extraBadge = {
                if (state.isExtraEnabled && !extraSlotIsNotEmpty && unreadExtraCount > 0) Badge()
            },
            objectBadge = {
                if (state.isObjectsEnabled && !objectSlotIsNotEmpty && unreadObjectsCount > 0) Badge()
            }
        ) {
            SupportingPaneScaffold(
                directive = directive,
                value = correctedValue,
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
                                    component = component,
                                    showDialog = component::doShowDialog,
                                    onFinishActivity = onFinish,
                                    doCreateSaveIntent = component::doCreateSaveIntent,
                                    doCreateLoadIntent = component::doCreateLoadIntent
                                )
                            }
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