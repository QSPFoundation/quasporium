package com.pixnpunk.settings.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.pixnpunk.settings.presentation.contents.SettingsAudioContent
import com.pixnpunk.settings.presentation.contents.SettingsGeneralContent
import com.pixnpunk.settings.presentation.contents.SettingsImageContent
import com.pixnpunk.settings.presentation.contents.SettingsRootContent
import com.pixnpunk.settings.presentation.contents.SettingsTextContent
import me.zhanghai.compose.preference.ProvidePreferenceLocals

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    component: SettingsComponent
) {
    val stack by component.childStack.subscribeAsState()
    val activeComponent = stack.active.instance
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    ProvidePreferenceLocals(
        flow = component.preferencesState
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                SettingsAppBar(
                    activeComponent = activeComponent,
                    scrollBehavior = scrollBehavior,
                    isLocalSettings = component.isLocalSettings,
                    gameTitle = component.gameTitle,
                    onClickBack = {
                        if (activeComponent is SettingsComponent.Child.RootChild) {
                            component.onDismissed()
                        } else {
                            component.backToRoot()
                        }
                    }
                )
            }
        ) { paddingValues ->
            Children(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                stack = stack
            ) { child ->
                when (child.instance) {
                    is SettingsComponent.Child.AudioChild ->
                        SettingsAudioContent()

                    is SettingsComponent.Child.GeneralChild ->
                        SettingsGeneralContent(
                            isLocalSettings = component.isLocalSettings
                        )

                    is SettingsComponent.Child.PicturesChild ->
                        SettingsImageContent(
                            showInputDialog = component.showInputDialog
                        )

                    is SettingsComponent.Child.TextChild ->
                        SettingsTextContent(
                            showColorDialog = component.showColorDialog,
                            resetToDefault = component.resetToDefault,
                            showFontDialog = component.showFontDialog
                        )

                    is SettingsComponent.Child.RootChild ->
                        SettingsRootContent(
                            isLocalSettings = component.isLocalSettings,
                            moveToChild = { component.moveToChild(it) }
                        )
                }
            }
        }
    }
}
