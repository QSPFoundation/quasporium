package com.pixnpunk.game.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.extra.presentation.ExtraContent
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameBottomBarStandard
import com.pixnpunk.main.presentation.MainContent
import com.pixnpunk.`object`.presentation.ObjectContent

@Composable
fun GameContentStandard(
    component: GameComponent,
    onFinish: () -> Unit
) {
    val state by component.model.collectAsState()

    val stack by component.childStandardStack.subscribeAsState()
    val activeComponent = stack.active.instance

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            GameAppBar(
                titleText =
                    when (activeComponent) {
                        is GameComponent.StandardChild.MainChild -> stringResource(R.string.tab_main)
                        is GameComponent.StandardChild.ExtraChild -> stringResource(R.string.tab_extra)
                        is GameComponent.StandardChild.ObjectChild -> stringResource(R.string.tab_object)
                    },
                component = component,
                showDialog = component::doShowDialog,
                onFinishActivity = onFinish,
                doCreateSaveIntent = component::doCreateSaveIntent,
                doCreateLoadIntent = component::doCreateLoadIntent
            )
        },
        bottomBar = {
            GameBottomBarStandard(
                isExtraEnabled = state.isExtraEnabled,
                isObjectsEnabled = state.isObjectsEnabled,
                activeComponent = activeComponent,
                navigationTo = component::standardNavigation
            )
        }
    ) { paddingValues ->
        Children(
            modifier = Modifier.padding(paddingValues),
            stack = stack
        ) { child ->
            when (val instance = child.instance) {
                is GameComponent.StandardChild.MainChild -> {
                    MainContent(instance.component)
                }

                is GameComponent.StandardChild.ExtraChild -> {
                    ExtraContent(instance.component)
                }

                is GameComponent.StandardChild.ObjectChild -> {
                    ObjectContent(instance.component)
                }
            }
        }
    }
}