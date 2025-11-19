package com.pixnpunk.game.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.dialogs.presentation.DialogsMainContent
import com.pixnpunk.extra.presentation.ExtraContent
import com.pixnpunk.game.presentation.component.GameAppBar
import com.pixnpunk.game.presentation.component.GameNavBar
import com.pixnpunk.game.presentation.theme.QuasporiumTheme
import com.pixnpunk.main.presentation.MainContent
import com.pixnpunk.`object`.presentation.ObjectContent
import com.pixnpunk.settings.SettingsRepository

@Composable
fun GameContent(
    isDarkTheme: Boolean,
    component: GameComponent,
    settingsRepository: SettingsRepository,
    onFinish: () -> Unit
) {
    val stack by component.childStack.subscribeAsState()
    val dialogSlot by component.dialogSlot.subscribeAsState()

    val activeComponent = stack.active.instance

    QuasporiumTheme(
        darkTheme = isDarkTheme
    ) {
        dialogSlot.child?.instance?.also {
            DialogsMainContent(it, settingsRepository)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                GameAppBar(
                    component = component,
                    showDialog = component::doShowDialog,
                    activeComponent = activeComponent,
                    onFinishActivity = onFinish
                )
            },
            bottomBar = { GameNavBar(component, activeComponent) }
        ) { paddingValues ->
            Children(
                modifier = Modifier.padding(paddingValues),
                stack = component.childStack
            ) { child ->
                when (val instance = child.instance) {
                    is GameComponent.Child.MainChild -> {
                        MainContent(instance.component)
                    }

                    is GameComponent.Child.ExtraChild -> {
                        ExtraContent(instance.component)
                    }

                    is GameComponent.Child.ObjectChild -> {
                        ObjectContent(instance.component)
                    }
                }
            }
        }
    }
}