package com.pixnpunk.settings.presentation

import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.dto.SettingsEntity
import com.pixnpunk.settings.SettingsRepository
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class RealSettingsComponent(
    private val componentContext: ComponentContext,
    override val showColorDialog: (ColorPickerState) -> Unit,
    override val showInputDialog: (InputState) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, SettingsComponent, KoinComponent {

    private val navigation = StackNavigation<ChildConfig>()

    override fun moveToChild(config: ChildConfig) {
        navigation.pushNew(config)
    }

    override fun backToRoot() {
        navigation.pop()
    }

    override fun writeDefault(state: ColorPickerState) =
        get<SettingsRepository>().updateColorValue(
            state = state,
            color = when (state) {
                ColorPickerState.COLOR_TEXT -> Color(SettingsEntity().textColor)
                ColorPickerState.COLOR_BACK -> Color(SettingsEntity().backColor)
                ColorPickerState.COLOR_LINK -> Color(SettingsEntity().linkColor)
            }
        )

    override val childStack: Value<ChildStack<*, SettingsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = ChildConfig.Root,
            handleBackButton = true,
            childFactory = { config, _ ->
                when (config) {
                    is ChildConfig.Audio -> {
                        SettingsComponent.Child.AudioChild()
                    }

                    is ChildConfig.General -> {
                        SettingsComponent.Child.GeneralChild()
                    }

                    is ChildConfig.Pictures -> {
                        SettingsComponent.Child.PicturesChild()
                    }

                    is ChildConfig.Root -> {
                        SettingsComponent.Child.RootChild()
                    }

                    is ChildConfig.Text -> {
                        SettingsComponent.Child.TextChild()
                    }
                }
            }
        )

    @Serializable
    sealed class ChildConfig {
        @Serializable
        data object Root : ChildConfig()

        @Serializable
        data object General : ChildConfig()

        @Serializable
        data object Text : ChildConfig()

        @Serializable
        data object Pictures : ChildConfig()

        @Serializable
        data object Audio : ChildConfig()
    }
}