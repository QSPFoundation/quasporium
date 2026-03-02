package com.pixnpunk.game.presentation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldDefaults
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffold
import androidx.compose.material3.adaptive.layout.SupportingPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldDestinationItem
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.layout.calculateThreePaneScaffoldValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    val state by component.model.collectAsState()
    val dialogSlot by component.dialogSlot.subscribeAsState()

    val extraSlot by component.extraSlot.subscribeAsState()
    val extraSlotIsNotEmpty = extraSlot.child != null

    val objectSlot by component.objectSlot.subscribeAsState()
    val objectSlotIsNotEmpty = objectSlot.child != null

    val directive = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo())

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
            cleanAllSlot = component::clearAllSlot
        ) {
            SupportingPaneScaffold(
                directive = directive,
                value = calculateThreePaneScaffoldValue(
                    maxHorizontalPartitions = directive.maxHorizontalPartitions,
                    maxVerticalPartitions = directive.maxVerticalPartitions,
                    adaptStrategies = ListDetailPaneScaffoldDefaults.adaptStrategies(),
                    destinationHistory = listOf(
                        ThreePaneScaffoldDestinationItem<Any>(
                            pane = when {
                                extraSlotIsNotEmpty -> SupportingPaneScaffoldRole.Supporting
                                objectSlotIsNotEmpty -> SupportingPaneScaffoldRole.Extra
                                else -> SupportingPaneScaffoldRole.Main
                            }
                        )
                    )
                ),
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
                    val child = extraSlot.child?.instance
                    if (child != null) {
                        AnimatedPane { ExtraContent(child, component::clearAllSlot) }
                    }
                },
                extraPane = {
                    val child = objectSlot.child?.instance
                    if (child != null) {
                        AnimatedPane { ObjectContent(child, component::clearAllSlot) }
                    }
                }
            )
        }
    }
}