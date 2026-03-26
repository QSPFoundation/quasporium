package com.pixnpunk.settings.presentation

import android.content.SharedPreferences
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.data.repository.SettingsRepository
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.parameter.parametersOf
import org.koin.core.qualifier.named

class RealSettingsComponent(
    private val componentContext: ComponentContext,
    private val gameId: Long = -1L,
    override val gameTitle: String = "",
    override val showColorDialog: (ColorPickerState) -> Unit,
    override val resetToDefault: (ColorPickerState) -> Unit,
    override val showInputDialog: (InputState) -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, SettingsComponent, KoinComponent {

    override val isLocalSettings: Boolean = gameId != -1L

    override val preferences: SharedPreferences by lazy {
        if (gameId == -1L) {
            get(named("GlobalPrefs"))
        } else {
            get(named("LocalPrefs")) { parametersOf(gameId) }
        }
    }

    override val repository: SettingsRepository = instanceKeeper.getOrCreate(key = "SettingsRepo_$gameId") {
        if (gameId == -1L) {
            getKoin().get(named("GlobalRepository"))
        } else {
            getKoin().get(named("GameRepository")) { parametersOf(gameId) }
        }
    }

    private val navigation = StackNavigation<ChildConfig>()

    override fun moveToChild(config: ChildConfig) = navigation.pushNew(config)

    override fun backToRoot() = navigation.pop()

    override val childStack: Value<ChildStack<*, SettingsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = null,
            initialConfiguration = ChildConfig.Root,
            handleBackButton = true,
            childFactory = { config, _ ->
                when (config) {
                    is ChildConfig.Audio -> SettingsComponent.Child.AudioChild()

                    is ChildConfig.General -> SettingsComponent.Child.GeneralChild()

                    is ChildConfig.Pictures -> SettingsComponent.Child.PicturesChild()

                    is ChildConfig.Root -> SettingsComponent.Child.RootChild()

                    is ChildConfig.Text -> SettingsComponent.Child.TextChild()
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