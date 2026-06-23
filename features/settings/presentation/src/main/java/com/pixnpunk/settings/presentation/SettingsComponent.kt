package com.pixnpunk.settings.presentation

import android.content.SharedPreferences
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.data.repository.SettingsRepository

sealed interface SettingsComponent {
    val childStack: Value<ChildStack<*, Child>>
    val onDismissed: () -> Unit

    val showColorDialog: (ColorPickerState) -> Unit
    val resetToDefault: (ColorPickerState) -> Unit
    val showInputDialog: (InputState) -> Unit
    val showFontDialog: () -> Unit

    val preferences: SharedPreferences
    val repository: SettingsRepository
    val isLocalSettings: Boolean

    val gameTitle: String

    fun moveToChild(config: RealSettingsComponent.ChildConfig)
    fun backToRoot()

    sealed class Child {
        class AudioChild : Child()
        class GeneralChild : Child()
        class PicturesChild : Child()
        class RootChild : Child()
        class TextChild : Child()
    }
}