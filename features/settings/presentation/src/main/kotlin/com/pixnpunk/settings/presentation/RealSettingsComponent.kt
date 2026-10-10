package com.pixnpunk.settings.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.pixnpunk.dto.ColorPickerState
import com.pixnpunk.dto.InputState
import com.pixnpunk.settings.dto.repository.SettingsRepository
import com.pixnpunk.settings.presentation.mappers.dispatch
import com.pixnpunk.settings.presentation.mappers.toMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import me.zhanghai.compose.preference.Preferences
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class RealSettingsComponent(
    private val componentContext: ComponentContext,
    private val gameId: Long? = null,
    override val gameTitle: String = "",
    override val showColorDialog: (ColorPickerState) -> Unit,
    override val showInputDialog: (InputState) -> Unit,
    override val showFontDialog: () -> Unit,
    override val onDismissed: () -> Unit
) : ComponentContext by componentContext, SettingsComponent, KoinComponent {

    private val scope = coroutineScope()

    private val navigation = StackNavigation<ChildConfig>()

    private val settingsRepository: SettingsRepository by inject { parametersOf(gameId ?: -1L) }

    override val isLocalSettings: Boolean = gameId != null

    override val preferencesState = MutableStateFlow<Preferences>(
        ProxyPreference(emptyMap()) { _, _ -> }
    )

    override val resetToDefault: (ColorPickerState) -> Unit = { state ->
        scope.launch { settingsRepository.restoreColorValue(state) }
    }

    fun updateColor(state: ColorPickerState, colorArgb: Int) {
        scope.launch { settingsRepository.updateColorValue(state, colorArgb) }
    }

    fun updateImageSize(state: InputState, value: Int) {
        scope.launch { settingsRepository.updateImageSize(state, value) }
    }

    fun updateCustomFontPath(path: String) {
        scope.launch { settingsRepository.updateCustomFontPath(path) }
    }

    init {
        lifecycle.doOnDestroy { settingsRepository.cancel() }

        settingsRepository.settingsFlow
            .map { it.toMap() }
            .flowOn(Dispatchers.Default)
            .onEach { stateMap ->
                preferencesState.value = ProxyPreference(
                    stateMap = stateMap,
                    onEdit = { key, value ->
                        if (value != null) {
                            scope.launch { settingsRepository.dispatch(key, value) }
                        }
                    }
                )
            }
            .launchIn(scope)
    }

    override fun moveToChild(config: ChildConfig) = navigation.pushNew(config)

    override fun backToRoot() = navigation.pop()

    override val childStack: Value<ChildStack<*, SettingsComponent.Child>> =
        childStack(
            source = navigation,
            serializer = serializer(),
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
    sealed interface ChildConfig {
        @Serializable
        data object Root : ChildConfig

        @Serializable
        data object General : ChildConfig

        @Serializable
        data object Text : ChildConfig

        @Serializable
        data object Pictures : ChildConfig

        @Serializable
        data object Audio : ChildConfig
    }
}
