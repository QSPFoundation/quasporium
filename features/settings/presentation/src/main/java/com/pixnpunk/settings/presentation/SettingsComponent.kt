package com.pixnpunk.settings.presentation

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState

sealed interface SettingsComponent {
    val childStack: Value<ChildStack<*, Child>>
    val onDismissed: () -> Unit

    val showColorDialog: (ColorPickerState) -> Unit
    val showInputDialog: (InputState) -> Unit

    fun moveToChild(config: RealSettingsComponent.ChildConfig)
    fun backToRoot()
    fun writeDefault(state: ColorPickerState)

    sealed class Child {
        class AudioChild() : Child()
        class GeneralChild() : Child()
        class PicturesChild() : Child()
        class RootChild() : Child()
        class TextChild() : Child()
    }
}