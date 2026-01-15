package com.pixnpunk.game.presentation

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.VerticalDragHandle
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.navigation.NavigableSupportingPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberSupportingPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.extra.presentation.ExtraContent
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameBottomBarExpand
import com.pixnpunk.main.presentation.MainContentActionsListHolder
import com.pixnpunk.main.presentation.MainContentWebViewHolder
import com.pixnpunk.`object`.presentation.ObjectContent

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun GameContentExpand(
    component: GameComponent,
    onFinish: () -> Unit
) {
    val state by component.model.collectAsState()

    val stack by component.childExpandStack.subscribeAsState()
    val activeComponent = stack.active.instance

    val scaffoldNavigator = rememberSupportingPaneScaffoldNavigator()

    GameBottomBarExpand(
        isExtraEnabled = state.isExtraEnabled,
        isObjectsEnabled = state.isObjectsEnabled,
        activeComponent = activeComponent,
        navigationTo = component::expandNavigation
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                GameAppBar(
                    titleText =
                        when (activeComponent) {
                            is GameComponent.ExpandChild.MainActionsChild -> stringResource(R.string.tab_main)
                            is GameComponent.ExpandChild.ExtraChild -> stringResource(R.string.tab_extra)
                            is GameComponent.ExpandChild.ObjectChild -> stringResource(R.string.tab_object)
                        },
                    component = component,
                    showDialog = component::doShowDialog,
                    onFinishActivity = onFinish,
                    doCreateSaveIntent = component::doCreateSaveIntent,
                    doCreateLoadIntent = component::doCreateLoadIntent
                )
            }
        ) { paddingValues ->
            NavigableSupportingPaneScaffold(
                modifier = Modifier.padding(paddingValues),
                navigator = scaffoldNavigator,
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
                        MainContentWebViewHolder(
                            component =
                                (stack.items
                                    .filter { child -> child.instance is GameComponent.ExpandChild.MainActionsChild }[0]
                                    .instance as GameComponent.ExpandChild.MainActionsChild).component
                        )
                    }
                },
                supportingPane = {
                    AnimatedPane {
                        Children(
                            stack = stack
                        ) { child ->
                            when (val instance = child.instance) {
                                is GameComponent.ExpandChild.MainActionsChild -> {
                                    MainContentActionsListHolder(instance.component)
                                }

                                is GameComponent.ExpandChild.ExtraChild -> {
                                    ExtraContent(instance.component)
                                }

                                is GameComponent.ExpandChild.ObjectChild -> {
                                    ObjectContent(instance.component)
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}